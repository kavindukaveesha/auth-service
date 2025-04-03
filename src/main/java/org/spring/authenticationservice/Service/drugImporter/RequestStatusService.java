package org.spring.authenticationservice.Service.drugImporter;


import org.spring.authenticationservice.model.Enum.RequestStatusEnum;
import org.spring.authenticationservice.model.drugImporter.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestStatusService {

    RequestStatus save(RequestStatus requestStatus);

    RequestStatus updateStatus(Long id, RequestStatusEnum status);

    Optional<RequestStatus> findById(Long id);

    List<RequestStatus> findByRequestId(Long requestId);

    List<RequestStatus> findByDrugImporterId(Long drugImporterId);

    Optional<RequestStatus> findByRequestIdAndDrugImporterId(Long requestId, Long drugImporterId);

    void deleteById(Long id);

    List<RequestStatus> findAll();
}
