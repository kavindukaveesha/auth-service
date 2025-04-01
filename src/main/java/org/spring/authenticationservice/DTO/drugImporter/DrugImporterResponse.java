package org.spring.authenticationservice.DTO.drugImporter;

public class DrugImporterResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String licenseNumber;
    private String nicotineProofFilePath;
    private String licenseProofFilePath;
    private boolean enabled;

    // Default constructor
    public DrugImporterResponse() {
    }

    // Getter and Setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter and Setter for address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Getter and Setter for licenseNumber
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    // Getter and Setter for nicotineProofFilePath
    public String getNicotineProofFilePath() {
        return nicotineProofFilePath;
    }

    public void setNicotineProofFilePath(String nicotineProofFilePath) {
        this.nicotineProofFilePath = nicotineProofFilePath;
    }

    // Getter and Setter for licenseProofFilePath
    public String getLicenseProofFilePath() {
        return licenseProofFilePath;
    }

    public void setLicenseProofFilePath(String licenseProofFilePath) {
        this.licenseProofFilePath = licenseProofFilePath;
    }

    // Getter and Setter for enabled
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // toString method
    @Override
    public String toString() {
        return "DrugImporterResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", nicotineProofFilePath='" + nicotineProofFilePath + '\'' +
                ", licenseProofFilePath='" + licenseProofFilePath + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}