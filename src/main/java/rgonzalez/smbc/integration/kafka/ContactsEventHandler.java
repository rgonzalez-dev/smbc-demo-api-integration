package rgonzalez.smbc.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rgonzalez.smbc.integration.config.KafkaConfig;
import rgonzalez.smbc.integration.model.BusinessEvent;
import rgonzalez.smbc.integration.model.Contact;
import rgonzalez.smbc.integration.model.SsnVerificationResult;
import rgonzalez.smbc.integration.repository.BusinessEventRepository;
import rgonzalez.smbc.integration.repository.SsnVerificationResultRepository;
import rgonzalez.smbc.integration.service.SsnVerificationService;

@Service
public class ContactsEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ContactsEventHandler.class);
    private final BusinessEventRepository businessEventRepository;
    private final SsnVerificationService ssnVerificationService;
    private final SsnVerificationResultRepository ssnVerificationResultRepository;
    private final KafkaTemplate<String, SsnVerificationResult> ssnVerificationKafkaTemplate;
    private final ObjectMapper objectMapper;

    public ContactsEventHandler(BusinessEventRepository businessEventRepository,
            SsnVerificationService ssnVerificationService,
            SsnVerificationResultRepository ssnVerificationResultRepository,
            KafkaTemplate<String, SsnVerificationResult> ssnVerificationKafkaTemplate) {
        this.businessEventRepository = businessEventRepository;
        this.ssnVerificationService = ssnVerificationService;
        this.ssnVerificationResultRepository = ssnVerificationResultRepository;
        this.ssnVerificationKafkaTemplate = ssnVerificationKafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Listens to the contacts topic (business-events) and processes incoming events
     * Guarantees message ordering through:
     * - Single threaded consumer (concurrency=1)
     * - Manual acknowledgment after successful processing
     * - Sequential processing across all partitions
     *
     * @param event          The BusinessEvent from the contacts-api
     * @param aggregateId    The message key (contact/aggregate id)
     * @param partition      The partition this message came from
     * @param offset         The offset of this message
     * @param acknowledgment Manual acknowledgment handler
     */
    @KafkaListener(topics = KafkaConfig.CONTACTS_TOPIC, containerFactory = "contactsKafkaListenerContainerFactory", groupId = "integration-service")
    @Transactional
    public void handleContactEvent(
            @Payload BusinessEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String aggregateId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            logger.info("Processing event from partition [{}] with offset [{}]. Event: {}",
                    partition, offset, event);

            event.setId(null);
            // Persist the event to database
            BusinessEvent persistedEvent = businessEventRepository.save(event);
            logger.debug("Event persisted to database with id [{}]", persistedEvent.getId());

            // Process the business event
            processBusinessEvent(event);

            // Manually acknowledge the message after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                logger.debug("Message acknowledged for event [{}] in partition [{}]",
                        event.getEventId(), partition);
            }

        } catch (Exception e) {
            logger.error("Error processing event [{}] from partition [{}]: {}",
                    event.getEventId(), partition, e.getMessage(), e);
            // Do not acknowledge on error - message will be retried
            throw new RuntimeException("Failed to process business event", e);
        }
    }

    /**
     * Process the incoming BusinessEvent from the contacts-api
     * This method can be extended to perform various integration tasks:
     * - Log events to a data warehouse
     * - Update aggregated views
     * - Trigger downstream processes
     * - Send notifications
     *
     * @param event The business event to process
     */
    private void processBusinessEvent(BusinessEvent event) {
        logger.info("Processing business event - Type: {}, Aggregate: {}, EventName: {}",
                event.getAggregateName(), event.getAggregateId(), event.getEventName());

        // Example processing based on event type
        switch (event.getEventName()) {
            case "ContactCreated":
                handleContactCreatedEvent(event);
                break;
            case "ContactUpdated":
                handleContactUpdatedEvent(event);
                break;
            case "ContactDeleted":
                handleContactDeletedEvent(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventName());
        }
    }

    /**
     * Handle ContactCreated event
     * Perform integration tasks when a contact is created
     * Triggers async SSN verification for the contact
     * Persists verification results and sends outcome to Kafka topic
     *
     * @param event The contact created event
     */
    private void handleContactCreatedEvent(BusinessEvent event) {
        logger.info("Handling ContactCreated event for contact [{}]", event.getAggregateId());

        try {
            // Parse the event payload to extract SSN and name information
            Contact contact = objectMapper.readValue(
                    event.getEventPayload(),
                    Contact.class);

            SsnVerificationRequest verificationRequest = new SsnVerificationRequest(
                    contact.getSsn(),
                    contact.getFirstName(),
                    contact.getLastName());

            logger.debug("Extracted verification request from payload: SSN={}, FirstName={}, LastName={}",
                    verificationRequest.getSsn(), verificationRequest.getFirstName(),
                    verificationRequest.getLastName());

            // Call the SSN verification service asynchronously
            ssnVerificationService.verifySSNMatch(
                    verificationRequest.getSsn(),
                    verificationRequest.getFirstName(),
                    verificationRequest.getLastName()).thenAccept(result -> {
                        logger.info("SSN verification completed for contact [{}]: Status={}, Matching={}",
                                event.getAggregateId(), result.status(), result.isMatching());

                        // Persist verification result to database
                        SsnVerificationResult verificationResult = new SsnVerificationResult(
                                event.getAggregateId(),
                                result.ssn(),
                                verificationRequest.getFirstName(),
                                verificationRequest.getLastName(),
                                result.status(),
                                result.isMatching(),
                                result.message(),
                                "KafkaEventHandler",
                                result.verificationTimestamp());

                        SsnVerificationResult persistedResult = ssnVerificationResultRepository
                                .save(verificationResult);
                        logger.info("SSN verification result persisted to database with id [{}]",
                                persistedResult.getId());

                        // Send verification outcome to Kafka topic
                        ssnVerificationKafkaTemplate.send(
                                KafkaConfig.CUSTOMER_SSN_VERIFIED_TOPIC,
                                event.getAggregateId(),
                                persistedResult).whenComplete((sendResult, exception) -> {
                                    if (exception == null) {
                                        logger.info(
                                                "SSN verification outcome sent to Kafka topic [{}] for contact [{}]",
                                                KafkaConfig.CUSTOMER_SSN_VERIFIED_TOPIC, event.getAggregateId());
                                    } else {
                                        logger.error(
                                                "Error sending SSN verification outcome to Kafka topic for contact [{}]",
                                                event.getAggregateId(), exception);
                                    }
                                });

                    }).exceptionally(ex -> {
                        logger.error("Error during SSN verification for contact [{}]: {}",
                                event.getAggregateId(), ex.getMessage(), ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Error parsing event payload for SSN verification: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle ContactUpdated event
     * Perform integration tasks when a contact is updated
     *
     * @param event The contact updated event
     */
    private void handleContactUpdatedEvent(BusinessEvent event) {
        logger.info("Handling ContactUpdated event for contact [{}]", event.getAggregateId());
        // Add your integration logic here
    }

    /**
     * Handle ContactDeleted event
     * Perform integration tasks when a contact is deleted
     *
     * @param event The contact deleted event
     */
    private void handleContactDeletedEvent(BusinessEvent event) {
        logger.info("Handling ContactDeleted event for contact [{}]", event.getAggregateId());
        // Add your integration logic here
    }
}
