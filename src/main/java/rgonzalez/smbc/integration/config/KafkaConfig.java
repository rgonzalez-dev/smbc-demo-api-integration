package rgonzalez.smbc.integration.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import rgonzalez.smbc.integration.model.BusinessEvent;
import rgonzalez.smbc.integration.model.SsnVerificationResult;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String CONTACTS_TOPIC = "contacts";
    public static final String CUSTOMER_VERIFICATIONS_TOPIC = "customer-verifications";
    public static final String CUSTOMER_SSN_VERIFIED_TOPIC = "customer-ssn-verified";
    public static final int PARTITIONS = 3;
    public static final short REPLICATION_FACTOR = 1;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Create the Contacts topic with 3 partitions
     * Only creates if kafka.auto-create-topics is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "kafka.auto-create-topics", havingValue = "true", matchIfMissing = false)
    public NewTopic contactsTopic() {
        return new NewTopic(CONTACTS_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    /**
     * Create the Customer Verifications topic with 3 partitions
     * Only creates if kafka.auto-create-topics is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "kafka.auto-create-topics", havingValue = "true", matchIfMissing = false)
    public NewTopic customerVerificationsTopic() {
        return new NewTopic(CUSTOMER_VERIFICATIONS_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    /**
     * Create the Customer SSN Verified topic with 3 partitions
     * Only creates if kafka.auto-create-topics is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "kafka.auto-create-topics", havingValue = "true", matchIfMissing = false)
    public NewTopic customerSsnVerifiedTopic() {
        return new NewTopic(CUSTOMER_SSN_VERIFIED_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    /**
     * Consumer Factory for Contact events
     * Guarantees message ordering by:
     * - Setting max.in.flight.requests.per.connection=1 to process one message at a
     * time per partition
     * - Disabling auto-commit to ensure offset is committed only after successful
     * processing
     */
    @Bean
    public ConsumerFactory<String, BusinessEvent> contactsConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildConsumerProperties());

        // Use ErrorHandlingDeserializer to wrap the actual deserializers
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Configure the wrapped deserializers
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "integration-service");

        // Message ordering guarantees
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1); // Process one message at a time
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual offset commit
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning if no offset

        // JsonDeserializer configuration for the wrapped instance
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // Disable type info headers since contacts-api doesn't send them
        // Instead, use the default type for deserialization
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "rgonzalez.smbc.integration.model.BusinessEvent");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka Listener Container Factory for processing contacts
     * Guarantees message ordering by setting concurrency=1 (single threaded)
     * This ensures messages from all partitions are processed sequentially
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BusinessEvent> contactsKafkaListenerContainerFactory(
            ConsumerFactory<String, BusinessEvent> contactsConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, BusinessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // Add error handler with exponential backoff
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        factory.setCommonErrorHandler(errorHandler);

        factory.setConcurrency(1); // Single threaded consumer for ordering
        factory.setConsumerFactory(contactsConsumerFactory);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL); // Manual acknowledgment
        return factory;
    }

    /**
     * Producer Factory for BusinessEvent with String serialization for keys and
     * JSON for values for customer verifications
     */
    @Bean
    public ProducerFactory<String, BusinessEvent> customerVerificationsProducerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for sending BusinessEvent messages to customer-verifications
     * topic
     * The aggregate id will be used as the message key
     */
    @Bean
    public KafkaTemplate<String, BusinessEvent> customerVerificationsKafkaTemplate(
            ProducerFactory<String, BusinessEvent> customerVerificationsProducerFactory) {
        return new KafkaTemplate<>(customerVerificationsProducerFactory);
    }

    /**
     * Producer Factory for SsnVerificationResult with String serialization for keys
     * and
     * JSON for values for customer SSN verification outcomes
     */
    @Bean
    public ProducerFactory<String, SsnVerificationResult> ssnVerificationResultProducerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for sending SsnVerificationResult messages to
     * customer-ssn-verified
     * topic
     * The contact id will be used as the message key
     */
    @Bean
    public KafkaTemplate<String, SsnVerificationResult> ssnVerificationKafkaTemplate(
            ProducerFactory<String, SsnVerificationResult> ssnVerificationResultProducerFactory) {
        return new KafkaTemplate<>(ssnVerificationResultProducerFactory);
    }
}
