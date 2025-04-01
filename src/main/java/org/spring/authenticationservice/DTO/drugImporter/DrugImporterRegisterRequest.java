package org.spring.authenticationservice.DTO.drugImporter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object for drug importer registration requests.
 * Contains all fields necessary for creating a new drug importer account
 * including personal information and required documentation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugImporterRegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$",
            message = "Phone number format is not valid")
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @Pattern(regexp = "^(https?:\\/\\/)?([\\w\\.-]+)\\.([a-z\\.]{2,6})([\\w\\.-]*)*\\/?$",
            message = "Website URL format is not valid")
    private String website;

    @Size(min = 5, max = 20, message = "NIC must be between 5 and 20 characters")
    private String nic;

    @Size(max = 1000, message = "Additional text cannot exceed 1000 characters")
    private String additionalText;

    // File upload fields
    private MultipartFile nicotineProofFile;
    private MultipartFile licenseProofFile;
}