package org.spring.authenticationservice.repository.drugImporter;

import org.spring.authenticationservice.model.drugImporter.DrugImporter;
import org.spring.authenticationservice.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DrugImporter entities
 * Provides methods for CRUD operations and custom queries
 */
@Repository
public interface DrugImporterRepository extends JpaRepository<DrugImporter, Long> {

    /**
     * Find a drug importer by activation token
     *
     * @param token The activation token
     * @return Optional containing the drug importer if found
     */
    Optional<DrugImporter> findByActivationToken(String token);

    /**
     * Find a drug importer by associated User entity
     *
     * @param user The User entity
     * @return Optional containing the drug importer if found
     */
    Optional<DrugImporter> findByUser(User user);

    /**
     * Check if a drug importer exists for a given user
     *
     * @param user The User entity
     * @return true if exists, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Find drug importers with names containing the given string (case insensitive)
     *
     * @param name The name to search for
     * @return List of matching drug importers
     */
    List<DrugImporter> findByNameContainingIgnoreCase(String name);

    /**
     * Find drug importers by license number
     *
     * @param licenseNumber The license number
     * @return Optional containing the drug importer if found
     */
    Optional<DrugImporter> findByLicenseNumber(String licenseNumber);

    /**
     * Check if a drug importer exists with the given license number
     *
     * @param licenseNumber The license number
     * @return true if exists, false otherwise
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Find all enabled drug importers (associated with enabled users)
     *
     * @return List of enabled drug importers
     */
    @Query("SELECT di FROM DrugImporter di JOIN di.user u WHERE u.isEnabled = true")
    List<DrugImporter> findAllEnabled();

    /**
     * Find all drug importers with expiring licenses (within specified days)
     *
     * @param days Number of days until expiration
     * @return List of drug importers with soon-to-expire licenses
     */
    @Query(value = "SELECT di.* FROM drug_importers di " +
            "WHERE DATEDIFF(di.license_expiry_date, CURDATE()) <= :days " +
            "AND DATEDIFF(di.license_expiry_date, CURDATE()) >= 0",
            nativeQuery = true)
    List<DrugImporter> findAllWithExpiringLicenses(@Param("days") int days);

    /**
     * Find drug importers by NIC
     *
     * @param nic The National Identity Card number
     * @return Optional containing the drug importer if found
     */
    Optional<DrugImporter> findByNic(String nic);

    /**
     * Find drug importers by phone number
     *
     * @param phone The phone number
     * @return List of matching drug importers
     */
    List<DrugImporter> findByPhone(String phone);
}