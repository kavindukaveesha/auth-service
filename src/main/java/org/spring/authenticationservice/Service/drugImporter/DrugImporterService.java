package org.spring.authenticationservice.Service.drugImporter;

import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRegisterRequest;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterUpdateRequest;
import org.spring.authenticationservice.model.drugImporter.DrugImporter;
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

// Interface
public interface DrugImporterService {
    DrugImporter registerDrugImporter(DrugImporterRegisterRequest registerDto,
                                      MultipartFile nicotineProofFile,
                                      MultipartFile licenseProofFile) throws Exception;

    DrugImporter findById(Long id) throws Exception;

    DrugImporter findByEmail(String email) throws Exception;

    List<DrugImporter> findAll();

    DrugImporter updateDrugImporter(Long id,
                                    DrugImporterUpdateRequest updateDto,
                                    MultipartFile nicotineProofFile,
                                    MultipartFile licenseProofFile) throws Exception;

    void deleteDrugImporter(Long id) throws Exception;

    void activateDrugImporter(String token) throws Exception;
}


