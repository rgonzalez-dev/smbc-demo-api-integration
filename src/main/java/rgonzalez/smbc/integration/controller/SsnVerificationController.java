package rgonzalez.smbc.integration.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rgonzalez.smbc.integration.service.SsnVerificationService;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for SSN verification operations.
 * Provides endpoints for asynchronous social security number verification.
 */
@RestController
@RequestMapping("/api/v1/ssn-verification")
public class SsnVerificationController {

    private static final Logger logger = LoggerFactory.getLogger(SsnVerificationController.class);

    @Autowired
    private SsnVerificationService ssnVerificationService;

    /**
     * Verify SSN with separate first and last names.
     * 
     * @param ssn       the social security number (format: XXX-XX-XXXX)
     * @param firstName the first name
     * @param lastName  the last name
     * @return CompletableFuture of verification result
     */
    @GetMapping("/verify")
    public CompletableFuture<ResponseEntity<SsnVerificationService.SsnVerificationResult>> verifySsnWithNames(
            @RequestParam String ssn,
            @RequestParam String firstName,
            @RequestParam String lastName) {

        logger.info("Received SSN verification request: SSN={}, FirstName={}, LastName={}", ssn, firstName, lastName);

        return ssnVerificationService.verifySSNMatch(ssn, firstName, lastName)
                .thenApply(result -> {
                    logger.info("SSN verification completed with result: {}", result.status());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(ex -> {
                    logger.error("Error during SSN verification", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * Verify SSN with full name.
     * 
     * @param ssn      the social security number
     * @param fullName the full name (first and last name)
     * @return CompletableFuture of verification result
     */
    @GetMapping("/verify-full-name")
    public CompletableFuture<ResponseEntity<SsnVerificationService.SsnVerificationResult>> verifySsnWithFullName(
            @RequestParam String ssn,
            @RequestParam String fullName) {

        logger.info("Received SSN verification request with full name: SSN={}, FullName={}", ssn, fullName);

        return ssnVerificationService.verifySSNMatch(ssn, fullName)
                .thenApply(result -> {
                    logger.info("SSN verification completed with result: {}", result.status());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(ex -> {
                    logger.error("Error during SSN verification", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * Health check endpoint for SSN verification service.
     * 
     * @return service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SSN Verification Service is running");
    }
}
