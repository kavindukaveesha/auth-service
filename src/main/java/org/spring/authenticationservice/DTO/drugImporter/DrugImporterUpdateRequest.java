package org.spring.authenticationservice.DTO.drugImporter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object for drug importer profile update requests.
 * Contains fields that can be updated for an existing drug importer account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugImporterUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$",
            message = "Phone number format is not valid")
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @Pattern(regexp = "^(https?:\\/\\/)?([\\w\\.-]+)\\.([a-z\\.]{2,6})([\\w\\.-]*)*\\/?$",
            message = "Website URL format is not valid")
    private String website;

    @Size(min = 5, max = 20, message = "NIC must be between 5 and 20 characters")
    private String nic;

    @Size(max = 1000, message = "Additional text cannot exceed 1000 characters")
    private String additionalText;

    // Flags for removing existing files
    private Boolean removeNicotineProof;
    private Boolean removeLicenseProof;

    // New files to upload
    private MultipartFile nicotineProofFile;
    private MultipartFile licenseProofFile;
}