package org.spring.authenticationservice.repository.drugImporter;

import org.spring.authenticationservice.model.drugImporter.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Long> {

    List<RequestStatus> findByRequestId(Long requestId);

    List<RequestStatus> findByDrugImporterId(Long drugImporterId);

    Optional<RequestStatus> findByRequestIdAndDrugImporterId(Long requestId, Long drugImporterId);
}
