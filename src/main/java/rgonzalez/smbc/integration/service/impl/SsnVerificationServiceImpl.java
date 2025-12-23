package rgonzalez.smbc.integration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rgonzalez.smbc.integration.service.SsnVerificationService;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Implementation of SSN verification service.
 * Provides async verification of social security numbers matching with names.
 */
@Service
public class SsnVerificationServiceImpl implements SsnVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(SsnVerificationServiceImpl.class);
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");

    /**
     * Asynchronously verify if a social security number matches the provided first
     * and last names.
     */
    @Override
    @Async
    public CompletableFuture<SsnVerificationResult> verifySSNMatch(String ssn, String firstName, String lastName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Starting SSN verification: SSN={}, FirstName={}, LastName={}", ssn, firstName, lastName);

            try {
                // Validate SSN format
                if (!isValidSSNFormat(ssn)) {
                    logger.warn("Invalid SSN format: {}", ssn);
                    return new SsnVerificationResult(
                            ssn,
                            firstName + " " + lastName,
                            false,
                            "INVALID_FORMAT",
                            "SSN format is invalid. Expected format: XXX-XX-XXXX",
                            System.currentTimeMillis());
                }

                // Validate names
                if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
                    logger.warn("Invalid names provided: FirstName={}, LastName={}", firstName, lastName);
                    return new SsnVerificationResult(
                            ssn,
                            firstName + " " + lastName,
                            false,
                            "INVALID_NAME",
                            "First name and last name must not be empty",
                            System.currentTimeMillis());
                }

                // Perform verification logic
                boolean isMatching = performVerification(ssn, firstName, lastName);

                String status = isMatching ? "VERIFIED" : "NOT_MATCHING";
                String message = isMatching ? "SSN matches the provided name" : "SSN does not match the provided name";

                logger.info("SSN verification completed: SSN={}, Status={}, Matching={}", ssn, status, isMatching);

                return new SsnVerificationResult(
                        ssn,
                        firstName + " " + lastName,
                        isMatching,
                        status,
                        message,
                        System.currentTimeMillis());

            } catch (Exception e) {
                logger.error("Error during SSN verification: SSN={}, FirstName={}, LastName={}", ssn, firstName,
                        lastName, e);
                return new SsnVerificationResult(
                        ssn,
                        firstName + " " + lastName,
                        false,
                        "ERROR",
                        "An error occurred during verification: " + e.getMessage(),
                        System.currentTimeMillis());
            }
        });
    }

    /**
     * Asynchronously verify if a social security number matches the provided full
     * name.
     */
    @Override
    @Async
    public CompletableFuture<SsnVerificationResult> verifySSNMatch(String ssn, String fullName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Starting SSN verification: SSN={}, FullName={}", ssn, fullName);

            try {
                // Validate SSN format
                if (!isValidSSNFormat(ssn)) {
                    logger.warn("Invalid SSN format: {}", ssn);
                    return new SsnVerificationResult(
                            ssn,
                            fullName,
                            false,
                            "INVALID_FORMAT",
                            "SSN format is invalid. Expected format: XXX-XX-XXXX",
                            System.currentTimeMillis());
                }

                // Validate name
                if (fullName == null || fullName.trim().isEmpty()) {
                    logger.warn("Invalid full name provided: {}", fullName);
                    return new SsnVerificationResult(
                            ssn,
                            fullName,
                            false,
                            "INVALID_NAME",
                            "Full name must not be empty",
                            System.currentTimeMillis());
                }

                // Parse full name into first and last name
                String[] nameParts = fullName.trim().split("\\s+", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : "";
                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                // Perform verification logic
                boolean isMatching = performVerification(ssn, firstName, lastName);

                String status = isMatching ? "VERIFIED" : "NOT_MATCHING";
                String message = isMatching ? "SSN matches the provided name" : "SSN does not match the provided name";

                logger.info("SSN verification completed: SSN={}, Status={}, Matching={}", ssn, status, isMatching);

                return new SsnVerificationResult(
                        ssn,
                        fullName,
                        isMatching,
                        status,
                        message,
                        System.currentTimeMillis());

            } catch (Exception e) {
                logger.error("Error during SSN verification: SSN={}, FullName={}", ssn, fullName, e);
                return new SsnVerificationResult(
                        ssn,
                        fullName,
                        false,
                        "ERROR",
                        "An error occurred during verification: " + e.getMessage(),
                        System.currentTimeMillis());
            }
        });
    }

    /**
     * Validate SSN format.
     * 
     * @param ssn the SSN to validate
     * @return true if SSN format is valid, false otherwise
     */
    private boolean isValidSSNFormat(String ssn) {
        if (ssn == null) {
            return false;
        }
        return SSN_PATTERN.matcher(ssn).matches();
    }

    /**
     * Perform the actual verification logic.
     * This is a placeholder that can be extended with actual verification logic
     * (e.g., calling an external SSN verification service, database lookup, etc.)
     * 
     * @param ssn       the SSN to verify
     * @param firstName the first name
     * @param lastName  the last name
     * @return true if SSN matches the name, false otherwise
     */
    private boolean performVerification(String ssn, String firstName, String lastName) {
        // TODO: Implement actual verification logic
        // This could involve:
        // - Calling an external SSN verification service
        // - Querying a database
        // - Validating against a trusted data source
        // For now, we'll implement a basic validation that can be extended

        logger.debug("Performing SSN verification: SSN={}, FirstName={}, LastName={}", ssn, firstName, lastName);

        try {
            // Simulate processing delay (e.g., external service call)
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.warn("SSN verification was interrupted", e);
            Thread.currentThread().interrupt();
        }

        // Basic placeholder logic - can be replaced with actual verification
        // In a real implementation, this would call an external service or database
        return !ssn.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty();
    }
}
