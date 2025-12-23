package rgonzalez.smbc.integration.model;

import java.time.LocalDateTime;

public class Contact {

    private Long id;
    private String name;
    private String ssn;
    private String firstName;
    private String lastName;
    private String middleInitial;
    private String createdBy;
    private LocalDateTime createdTimestamp;
    private String updatedBy;
    private LocalDateTime updatedTimestamp;

    /*
     * @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval =
     * true, fetch = FetchType.LAZY)
     * private Set<Phone> phones = new HashSet<>();
     * 
     * @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval =
     * true, fetch = FetchType.LAZY)
     * private Set<Email> emails = new HashSet<>();
     * 
     * @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval =
     * true, fetch = FetchType.LAZY)
     * private Set<Address> addresses = new HashSet<>();
     */
    // Constructors
    public Contact() {
    }

    public Contact(String name, String ssn, String firstName, String lastName, String middleInitial) {
        this.name = name;
        this.ssn = ssn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    /*
     * public Set<Phone> getPhones() {
     * return phones;
     * }
     * 
     * public void setPhones(Set<Phone> phones) {
     * this.phones = phones;
     * }
     * 
     * public Set<Email> getEmails() {
     * return emails;
     * }
     * 
     * public void setEmails(Set<Email> emails) {
     * this.emails = emails;
     * }
     * 
     * public Set<Address> getAddresses() {
     * return addresses;
     * }
     * 
     * public void setAddresses(Set<Address> addresses) {
     * this.addresses = addresses;
     * }
     * 
     * // Helper methods for relationships
     * public void addPhone(Phone phone) {
     * phones.add(phone);
     * phone.setContact(this);
     * }
     * 
     * public void removePhone(Phone phone) {
     * phones.remove(phone);
     * phone.setContact(null);
     * }
     * 
     * public void addEmail(Email email) {
     * emails.add(email);
     * email.setContact(this);
     * }
     * 
     * public void removeEmail(Email email) {
     * emails.remove(email);
     * email.setContact(null);
     * }
     * 
     * public void addAddress(Address address) {
     * addresses.add(address);
     * address.setContact(this);
     * }
     * 
     * public void removeAddress(Address address) {
     * addresses.remove(address);
     * address.setContact(null);
     * }
     */
}
