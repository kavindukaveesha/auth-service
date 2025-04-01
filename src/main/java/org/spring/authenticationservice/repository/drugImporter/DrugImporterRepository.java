package org.spring.authenticationservice.repository;


import org.spring.authenticationservice.model.drugImporter.DrugImporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrugImporterRepository extends JpaRepository<DrugImporter, Long> {
    // Use JPQL query to check if email exists in the related User entity
    @Query("SELECT COUNT(d) > 0 FROM DrugImporter d JOIN d.user u WHERE u.email = :email")
    boolean existsByUserEmail(@Param("email") String email);

    // This method is fine as it uses a path expression to navigate to the user's email
    Optional<DrugImporter> findByUserEmail(String email);
}
