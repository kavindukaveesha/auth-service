package org.spring.authenticationservice.controller;

import org.spring.authenticationservice.DTO.api.ApiResponse;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRegisterRequest;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterUpdateRequest;
import org.spring.authenticationservice.Service.drugImporter.DrugImporterService;
import org.spring.authenticationservice.Utils.SecurityUtil;
import org.spring.authenticationservice.model.drugImporter.DrugImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for managing Drug Importer operations
 * This controller handles registration, profile management, and administrative functions
 * related to drug importers in the authentication service.
 */
@RestController
@RequestMapping("/drug-importer")
public class DrugImporterController {

    // Define logger manually instead of using Lombok's @Slf4j to avoid compilation issues
    private static final Logger log = LoggerFactory.getLogger(DrugImporterController.class);

    // Maximum allowed file size for uploads (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Inject the service responsible for drug importer business logic
    @Autowired
    private DrugImporterService drugImporterService;

    // Inject utility for accessing security context information
    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Registers a new drug importer with required documentation
     *
     * @param request The registration data including personal info and proof documents
     * @return Response with registration result
     * @throws Exception If registration fails for any reason
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> registerDrugImporter(
            @ModelAttribute @Valid DrugImporterRegisterRequest request,
            @RequestPart(value = "nicotineProofFile", required = false) MultipartFile nicotineProofFile,
            @RequestPart(value = "licenseProofFile", required = false) MultipartFile licenseProofFile) throws Exception {

        log.info("Received drug importer registration request with file uploads");
        log.debug("Drug Importer details: {}", request);
        logFileDetails(nicotineProofFile, "Nicotine proof");
        logFileDetails(licenseProofFile, "License proof");

        validateFileSize(nicotineProofFile);
        validateFileSize(licenseProofFile);

        drugImporterService.registerDrugImporter(
                request,
                nicotineProofFile,
                licenseProofFile
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Drug Importer registered successfully. Account activation email has been sent."));
    }
    /**
     * Activates a drug importer account using the provided token
     *
     * @param token The activation token sent to user's email
     * @return Response with activation result
     * @throws Exception If activation fails
     */
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestParam String token) throws Exception {
        drugImporterService.activateDrugImporter(token);
        return ResponseEntity.ok(new ApiResponse(true, "Account activated successfully. You can now login."));
    }

    /**
     * Retrieves the profile of the currently authenticated drug importer
     *
     * @return The drug importer profile data
     * @throws Exception If profile retrieval fails
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRUG_IMPORTER')")
    public ResponseEntity<DrugImporter> getCurrentUserProfile() throws Exception {
        // Get username (email) from security context
        String email = securityUtil.getUsername();
        if (email == null) {
            throw new SecurityException("User not authenticated");
        }

        // Find and return the drug importer profile
        DrugImporter drugImporter = drugImporterService.findByEmail(email);
        return ResponseEntity.ok(drugImporter);
    }

    /**
     * Admin endpoint to retrieve all drug importers
     *
     * @return List of all drug importers
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DrugImporter>> getAllDrugImporters() {
        List<DrugImporter> importers = drugImporterService.findAll();
        return ResponseEntity.ok(importers);
    }

    /**
     * Admin endpoint to retrieve a specific drug importer by ID
     *
     * @param id The drug importer ID
     * @return The drug importer profile
     * @throws Exception If importer not found or other errors
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrugImporter> getDrugImporterById(@PathVariable Long id) throws Exception {
        DrugImporter importer = drugImporterService.findById(id);
        return ResponseEntity.ok(importer);
    }

    /**
     * Updates the current drug importer's profile
     *
     * @param request The update data with optional file uploads
     * @return The updated drug importer profile
     * @throws Exception If update fails
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DRUG_IMPORTER')")
    public ResponseEntity<DrugImporter> updateCurrentUserProfile(
            @ModelAttribute @Valid DrugImporterUpdateRequest request,
            @RequestPart(value = "nicotineProofFile", required = false) MultipartFile nicotineProofFile,
            @RequestPart(value = "licenseProofFile", required = false) MultipartFile licenseProofFile) throws Exception {

        // Get username (email) from security context
        String email = securityUtil.getUsername();
        if (email == null) {
            throw new SecurityException("User not authenticated");
        }

        // Find the drug importer profile
        DrugImporter drugImporter = drugImporterService.findByEmail(email);

        // Validate file sizes
        validateFileSize(nicotineProofFile);
        validateFileSize(licenseProofFile);

        // Update profile through service
        DrugImporter updatedDrugImporter = drugImporterService.updateDrugImporter(
                drugImporter.getId(), request, nicotineProofFile, licenseProofFile);

        return ResponseEntity.ok(updatedDrugImporter);
    }
    /**
     * Deletes the current drug importer's account
     *
     * @return Response confirming deletion
     * @throws Exception If deletion fails
     */
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('DRUG_IMPORTER')")
    public ResponseEntity<ApiResponse> deleteCurrentUserAccount() throws Exception {
        // Get username (email) from security context
        String email = securityUtil.getUsername();
        if (email == null) {
            throw new SecurityException("User not authenticated");
        }

        // Find and delete the drug importer
        DrugImporter drugImporter = drugImporterService.findByEmail(email);
        drugImporterService.deleteDrugImporter(drugImporter.getId());

        return ResponseEntity.ok(new ApiResponse(true, "Your account has been deleted successfully"));
    }

    /**
     * Admin endpoint to delete a drug importer by ID
     *
     * @param id The drug importer ID to delete
     * @return Response confirming deletion
     * @throws Exception If deletion fails
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteDrugImporter(@PathVariable Long id) throws Exception {
        drugImporterService.deleteDrugImporter(id);
        return ResponseEntity.ok(new ApiResponse(true, "Drug Importer deleted successfully"));
    }

    /**
     * Validates that uploaded files don't exceed maximum size
     *
     * @param file The file to validate
     * @throws IllegalArgumentException If file exceeds maximum size
     */
    private void validateFileSize(MultipartFile file) {
        if (file != null && !file.isEmpty() && file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }
    }

    /**
     * Logs details about uploaded files for debugging
     *
     * @param file The file to log details for
     * @param fileType Description of file type
     */
    private void logFileDetails(MultipartFile file, String fileType) {
        if (file != null && !file.isEmpty()) {
            log.debug("{} file: {}, size: {}", fileType, file.getOriginalFilename(), file.getSize());
        }
    }
}