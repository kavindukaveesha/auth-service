package org.spring.authenticationservice.Service.drugImporter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRegisterRequest;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterUpdateRequest;

import org.spring.authenticationservice.Service.drugImporter.DrugImporterService;
import org.spring.authenticationservice.Service.security.EmailService;
import org.spring.authenticationservice.exception.InvalidTokenException;
import org.spring.authenticationservice.exception.ResourceNotFoundException;
import org.spring.authenticationservice.model.drugImporter.DrugImporter;
import org.spring.authenticationservice.model.security.Role;
import org.spring.authenticationservice.model.security.User;
import org.spring.authenticationservice.repository.drugImporter.DrugImporterRepository;
import org.spring.authenticationservice.repository.security.RoleRepository;
import org.spring.authenticationservice.repository.security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the DrugImporterService interface
 */
@Service
public class DrugImporterServiceImpl implements DrugImporterService {

    private static final Logger log = LoggerFactory.getLogger(DrugImporterServiceImpl.class);

    // Token validity period in hours
    private static final int TOKEN_VALIDITY_HOURS = 24;

    @Autowired
    private DrugImporterRepository drugImporterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public DrugImporter registerDrugImporter(DrugImporterRegisterRequest request) throws Exception {
        log.info("Registering new drug importer with email: {}", request.getEmail());

        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS);

        // Create User entity for authentication
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true); //  should change this into false when use email sending

        // Assign DRUG_IMPORTER role
        Role drugImporterRole;
        Optional<Role> existingRole = Optional.ofNullable(roleRepository.findByName("DRUG_IMPORTER"));

        if (existingRole.isPresent()) {
            drugImporterRole = existingRole.get();
        } else {
            // Role doesn't exist, create it
            log.info("DRUG_IMPORTER not found, creating new role");
            Role newRole = new Role();
            newRole.setName("DRUG_IMPORTER");
            drugImporterRole = roleRepository.save(newRole);
        }
        List<Role> roles = new ArrayList<>();
        roles.add(drugImporterRole);
        user.setRoles(roles);

        // Save User
        User savedUser = userRepository.save(user);

        // Create drug importer entity
        DrugImporter drugImporter = DrugImporter.builder()
                .name(request.getName())
                .user(savedUser)
                .phone(request.getPhone())
                .address(request.getAddress())
                .licenseNumber(request.getLicenseNumber())
                .website(request.getWebsite())
                .nic(request.getNic())
                .additionalText(request.getAdditionalText())
                .nicotineProofUrl(request.getNicotineProofUrl())
                .licenseProofUrl(request.getLicenseProofUrl())
                .activationToken(activationToken)
                .activationTokenExpiry(tokenExpiry)
                .build();

        // Save to database
        DrugImporter savedDrugImporter = drugImporterRepository.save(drugImporter);
        log.info("Drug importer registered successfully with ID: {}", savedDrugImporter.getId());

        // Send activation email
//        emailService.sendEmail(savedUser.getEmail(), activationToken);
//        log.info("Activation email sent to: {}", savedUser.getEmail());

        return savedDrugImporter;
    }

    @Override
    @Transactional
    public void activateDrugImporter(String token) throws Exception {
        log.info("Activating drug importer account with token: {}", token);

        // Find drug importer by token
        DrugImporter drugImporter = drugImporterRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid activation token"));

        // Check if token is expired
        if (drugImporter.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Activation token has expired");
        }

        // Activate user account
        User user = drugImporter.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Clear activation token
        drugImporter.setActivationToken(null);
        drugImporter.setActivationTokenExpiry(null);
        drugImporterRepository.save(drugImporter);

        log.info("Drug importer account activated: {}", user.getEmail());
    }

    @Override
    public DrugImporter findByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return drugImporterRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Drug importer not found for user with email: " + email));
    }

    @Override
    public DrugImporter findById(Long id) throws Exception {
        return drugImporterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drug importer not found with ID: " + id));
    }

    @Override
    public List<DrugImporter> findAll() {
        return drugImporterRepository.findAll();
    }

    @Override
    @Transactional
    public DrugImporter updateDrugImporter(Long id, DrugImporterUpdateRequest request) throws ResourceNotFoundException {
        log.info("Updating drug importer with ID: {}", id);

        // Find drug importer
        DrugImporter drugImporter = drugImporterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drug importer not found with ID: " + id));

        User user = drugImporter.getUser();

        // Update fields if provided in request
        if (request.getName() != null) {
            drugImporter.setName(request.getName());
        }

        // If email is changing, check that new email is not in use
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        if (request.getPhone() != null) {
            drugImporter.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            drugImporter.setAddress(request.getAddress());
        }

        if (request.getLicenseNumber() != null) {
            drugImporter.setLicenseNumber(request.getLicenseNumber());
        }

        if (request.getWebsite() != null) {
            drugImporter.setWebsite(request.getWebsite());
        }

        if (request.getNic() != null) {
            drugImporter.setNic(request.getNic());
        }

        if (request.getAdditionalText() != null) {
            drugImporter.setAdditionalText(request.getAdditionalText());
        }

        // Handle document URLs
        if (request.getNicotineProofUrl() != null) {
            drugImporter.setNicotineProofUrl(request.getNicotineProofUrl());
        } else if (Boolean.TRUE.equals(request.getRemoveNicotineProof())) {
            drugImporter.setNicotineProofUrl(null);
        }

        if (request.getLicenseProofUrl() != null) {
            drugImporter.setLicenseProofUrl(request.getLicenseProofUrl());
        } else if (Boolean.TRUE.equals(request.getRemoveLicenseProof())) {
            drugImporter.setLicenseProofUrl(null);
        }

        return drugImporterRepository.save(drugImporter);
    }

    @Override
    @Transactional
    public void deleteDrugImporter(Long id) throws Exception {
        DrugImporter drugImporter = findById(id);
        User user = drugImporter.getUser();

        // Delete drug importer profile first (due to foreign key constraint)
        drugImporterRepository.delete(drugImporter);

        // Then delete the user account
        userRepository.delete(user);

        log.info("Drug importer with ID {} and associated user account deleted", id);
    }
}