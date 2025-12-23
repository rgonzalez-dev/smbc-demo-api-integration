package rgonzalez.smbc.integration.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for verifying social security numbers matching with names.
 * Provides async verification capabilities for SSN and name validation.
 */
public interface SsnVerificationService {

    /**
     * Asynchronously verify if a social security number matches the provided name.
     * 
     * @param ssn       the social security number to verify (format: XXX-XX-XXXX)
     * @param firstName the first name to match against
     * @param lastName  the last name to match against
     * @return CompletableFuture<SsnVerificationResult> containing the verification
     *         result
     */
    CompletableFuture<SsnVerificationResult> verifySSNMatch(String ssn, String firstName, String lastName);

    /**
     * Asynchronously verify if a social security number matches the provided full
     * name.
     * 
     * @param ssn      the social security number to verify
     * @param fullName the full name to match against
     * @return CompletableFuture<SsnVerificationResult> containing the verification
     *         result
     */
    CompletableFuture<SsnVerificationResult> verifySSNMatch(String ssn, String fullName);

    /**
     * Result object containing SSN verification details.
     */
    record SsnVerificationResult(
            String ssn,
            String name,
            boolean isMatching,
            String status,
            String message,
            long verificationTimestamp) {
    }
}
