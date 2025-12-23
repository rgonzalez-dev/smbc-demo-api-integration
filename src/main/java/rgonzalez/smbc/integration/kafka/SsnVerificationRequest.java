package rgonzalez.smbc.integration.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for SSN verification request data extracted from contact event payload.
 * Contains SSN and name information needed for verification.
 */
public class SsnVerificationRequest {

    @JsonProperty("ssn")
    private String ssn;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    // Constructors
    public SsnVerificationRequest() {
    }

    public SsnVerificationRequest(String ssn, String firstName, String lastName) {
        this.ssn = ssn;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "SsnVerificationRequest{" +
                "ssn='" + ssn + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
