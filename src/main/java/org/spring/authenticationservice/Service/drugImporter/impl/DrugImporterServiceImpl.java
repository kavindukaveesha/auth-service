package org.spring.authenticationservice.Service.drugImporter.impl;

import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRegisterRequest;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterUpdateRequest;
import org.spring.authenticationservice.Service.drugImporter.DrugImporterService;
import org.spring.authenticationservice.Service.security.EmailService;
import org.spring.authenticationservice.Service.security.JwtService;
import org.spring.authenticationservice.exceptions.AppExceptions;
import org.spring.authenticationservice.model.drugImporter.DrugImporter;
import org.spring.authenticationservice.model.security.Role;
import org.spring.authenticationservice.model.security.User;
import org.spring.authenticationservice.repository.DrugImporterRepository;
import org.spring.authenticationservice.repository.security.RoleRepository;
import org.spring.authenticationservice.repository.security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DrugImporterServiceImpl implements DrugImporterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DrugImporterRepository drugImporterRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads");

    public DrugImporterServiceImpl() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new AppExceptions.FileStorageException("Could not create the directory where files will be stored.", e);
        }
    }

    @Transactional
    public DrugImporter registerDrugImporter(DrugImporterRegisterRequest registerDto,
                                             MultipartFile nicotineProofFile,
                                             MultipartFile licenseProofFile) throws Exception {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(registerDto.getEmail())) {
                throw new AppExceptions.ResourceAlreadyExistsException("User with email " + registerDto.getEmail() + " already exists");
            }

            // 1. Create and save the User entity first
            User user = new User();
            user.setEmail(registerDto.getEmail());
            user.setPassword(encoder.encode(registerDto.getPassword()));
            user.setEnabled(false); // Require activation

            // Assign DRUG_IMPORTER role
            Role importerRole = roleRepository.findByName("DRUG_IMPORTER");

            user.getRoles().add(importerRole);

            // Save the user entity to get an ID
            user = userRepository.save(user);

            // 2. Create the DrugImporter entity with reference to the User
            DrugImporter drugImporter = new DrugImporter(user);

            // Set DrugImporter-specific fields
            drugImporter.setName(registerDto.getName());
            drugImporter.setPhone(registerDto.getPhone());
            drugImporter.setAddress(registerDto.getAddress());
            drugImporter.setLicenseNumber(registerDto.getLicenseNumber());
            drugImporter.setWebsite(registerDto.getWebsite());
            drugImporter.setNic(registerDto.getNic());
            drugImporter.setAdditionalText(registerDto.getAdditionalText());

            // Handle file uploads
            try {
                if (nicotineProofFile != null && !nicotineProofFile.isEmpty()) {
                    String nicotineProofPath = saveFile(nicotineProofFile, "nicotine_" + UUID.randomUUID());
                    drugImporter.setNicotineProofFilePath(nicotineProofPath);
                }

                if (licenseProofFile != null && !licenseProofFile.isEmpty()) {
                    String licenseProofPath = saveFile(licenseProofFile, "license_" + UUID.randomUUID());
                    drugImporter.setLicenseProofFilePath(licenseProofPath);
                }
            } catch (IOException e) {
                // If file upload fails, delete the user entity we just created
                userRepository.delete(user);
                throw new AppExceptions.FileStorageException("Failed to upload files", e);
            }

            // Save the drug importer entity
            drugImporter = drugImporterRepository.save(drugImporter);

            // Generate activation token and send email
            String activationToken = jwtService.generateActivationToken(user.getEmail());

            Map<String, String> emailBody = Map.of(
                    "to", user.getEmail(),
                    "name", drugImporter.getName(),
                    "activationLink", "localhost:8080/api/v1/drug-importer/activate?token=" + activationToken
            );

            try {
                emailService.sendEmail("activation", emailBody);
            } catch (Exception e) {
                // Log the error but don't roll back the transaction
                System.err.println("Failed to send activation email: " + e.getMessage());
                throw new AppExceptions.EmailSendingException("Registration successful, but email could not be sent", e);
            }

            return drugImporter;
        } catch (AppExceptions.ResourceAlreadyExistsException | AppExceptions.ResourceNotFoundException |
                 AppExceptions.FileStorageException | AppExceptions.EmailSendingException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to register drug importer: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public DrugImporter findById(Long id) throws Exception {
        try {
            return drugImporterRepository.findById(id)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Drug Importer not found with id: " + id));
        } catch (AppExceptions.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to find drug importer: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public DrugImporter findByEmail(String email) throws Exception {
        try {
            return drugImporterRepository.findByUserEmail(email)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Drug Importer not found with email: " + email));
        } catch (AppExceptions.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to find drug importer: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<DrugImporter> findAll() {
        return drugImporterRepository.findAll();
    }

    @Transactional
    public DrugImporter updateDrugImporter(Long id,
                                           DrugImporterUpdateRequest updateDto,
                                           MultipartFile nicotineProofFile,
                                           MultipartFile licenseProofFile) throws Exception {
        try {
            // Find the drug importer
            DrugImporter drugImporter = drugImporterRepository.findById(id)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Drug Importer not found with id: " + id));

            User user = drugImporter.getUser();

            // Update email if provided and different
            if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
                // Check if the new email is already in use
                if (userRepository.existsByEmail(updateDto.getEmail())) {
                    throw new AppExceptions.ResourceAlreadyExistsException("Email " + updateDto.getEmail() + " is already in use");
                }
                user.setEmail(updateDto.getEmail());
                userRepository.save(user);
            }

            // Update drug importer fields if provided
            if (updateDto.getName() != null) {
                drugImporter.setName(updateDto.getName());
            }

            if (updateDto.getPhone() != null) {
                drugImporter.setPhone(updateDto.getPhone());
            }

            if (updateDto.getAddress() != null) {
                drugImporter.setAddress(updateDto.getAddress());
            }

            if (updateDto.getLicenseNumber() != null) {
                drugImporter.setLicenseNumber(updateDto.getLicenseNumber());
            }

            if (updateDto.getWebsite() != null) {
                drugImporter.setWebsite(updateDto.getWebsite());
            }

            if (updateDto.getNic() != null) {
                drugImporter.setNic(updateDto.getNic());
            }

            if (updateDto.getAdditionalText() != null) {
                drugImporter.setAdditionalText(updateDto.getAdditionalText());
            }

            // Handle file removals
            if (Boolean.TRUE.equals(updateDto.getRemoveNicotineProof())) {
                String oldPath = drugImporter.getNicotineProofFilePath();
                if (oldPath != null) {
                    deleteFile(oldPath);
                    drugImporter.setNicotineProofFilePath(null);
                }
            }

            if (Boolean.TRUE.equals(updateDto.getRemoveLicenseProof())) {
                String oldPath = drugImporter.getLicenseProofFilePath();
                if (oldPath != null) {
                    deleteFile(oldPath);
                    drugImporter.setLicenseProofFilePath(null);
                }
            }

            // Handle file uploads
            try {
                if (nicotineProofFile != null && !nicotineProofFile.isEmpty()) {
                    // Delete old file if exists
                    String oldPath = drugImporter.getNicotineProofFilePath();
                    if (oldPath != null) {
                        deleteFile(oldPath);
                    }

                    // Save new file
                    String nicotineProofPath = saveFile(nicotineProofFile, "nicotine_" + UUID.randomUUID());
                    drugImporter.setNicotineProofFilePath(nicotineProofPath);
                }

                if (licenseProofFile != null && !licenseProofFile.isEmpty()) {
                    // Delete old file if exists
                    String oldPath = drugImporter.getLicenseProofFilePath();
                    if (oldPath != null) {
                        deleteFile(oldPath);
                    }

                    // Save new file
                    String licenseProofPath = saveFile(licenseProofFile, "license_" + UUID.randomUUID());
                    drugImporter.setLicenseProofFilePath(licenseProofPath);
                }
            } catch (IOException e) {
                throw new AppExceptions.FileStorageException("Failed to upload files", e);
            }

            // Save the updated drug importer
            return drugImporterRepository.save(drugImporter);
        } catch (AppExceptions.ResourceNotFoundException | AppExceptions.ResourceAlreadyExistsException |
                 AppExceptions.FileStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to update drug importer: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteDrugImporter(Long id) throws Exception {
        try {
            // Find the drug importer
            DrugImporter drugImporter = drugImporterRepository.findById(id)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Drug Importer not found with id: " + id));

            // Get the associated user
            User user = drugImporter.getUser();

            // Delete files if they exist
            try {
                if (drugImporter.getNicotineProofFilePath() != null) {
                    deleteFile(drugImporter.getNicotineProofFilePath());
                }

                if (drugImporter.getLicenseProofFilePath() != null) {
                    deleteFile(drugImporter.getLicenseProofFilePath());
                }
            } catch (IOException e) {
                // Log but continue with deletion
                System.err.println("Failed to delete files: " + e.getMessage());
            }

            // Delete the drug importer
            drugImporterRepository.delete(drugImporter);

            // Delete the user
            userRepository.delete(user);
        } catch (AppExceptions.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to delete drug importer: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void activateDrugImporter(String token) throws Exception {
        try {
            // Validate and extract email from token
            String email = String.valueOf(jwtService.validateToken(token));
            if (email == null) {
                throw new AppExceptions.InvalidTokenException("Invalid or expired activation token");
            }

            // Find the user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found with email: " + email));

            // Check if already activated
            if (user.isEnabled()) {
                throw new AppExceptions.ResourceAlreadyExistsException("Account is already activated");
            }

            // Activate the user
            user.setEnabled(true);
            userRepository.save(user);
        } catch (AppExceptions.InvalidTokenException | AppExceptions.ResourceNotFoundException |
                 AppExceptions.ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to activate account: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private String saveFile(MultipartFile file, String filenamePrefix) throws IOException {
        // Normalize file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = filenamePrefix + fileExtension;

        // Copy file to the target location
        Path targetLocation = fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    private void deleteFile(String filename) throws IOException {
        if (StringUtils.hasText(filename)) {
            Path filePath = fileStorageLocation.resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }
}
