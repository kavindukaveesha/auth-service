package org.spring.authenticationservice.Service.drugImporter.impl;

import org.spring.authenticationservice.Service.drugImporter.RequestStatusService;
import org.spring.authenticationservice.model.Enum.RequestStatusEnum;
import org.spring.authenticationservice.model.drugImporter.RequestStatus;
import org.spring.authenticationservice.repository.drugImporter.RequestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RequestStatusServiceImpl implements RequestStatusService {

    private final RequestStatusRepository requestStatusRepository;

    @Autowired
    public RequestStatusServiceImpl(RequestStatusRepository requestStatusRepository) {
        this.requestStatusRepository = requestStatusRepository;
    }

    @Override
    @Transactional
    public RequestStatus save(RequestStatus requestStatus) {
        return requestStatusRepository.save(requestStatus);
    }

    @Override
    @Transactional
    public RequestStatus updateStatus(Long id, RequestStatusEnum status) {
        RequestStatus requestStatus = requestStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request status not found with id: " + id));

        requestStatus.setStatus(status);
        return requestStatusRepository.save(requestStatus);
    }

    @Override
    public Optional<RequestStatus> findById(Long id) {
        return requestStatusRepository.findById(id);
    }

    @Override
    public List<RequestStatus> findByRequestId(Long requestId) {
        return requestStatusRepository.findByRequestId(requestId);
    }

    @Override
    public List<RequestStatus> findByDrugImporterId(Long drugImporterId) {
        return requestStatusRepository.findByDrugImporterId(drugImporterId);
    }

    @Override
    public Optional<RequestStatus> findByRequestIdAndDrugImporterId(Long requestId, Long drugImporterId) {
        return requestStatusRepository.findByRequestIdAndDrugImporterId(requestId, drugImporterId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        requestStatusRepository.deleteById(id);
    }

    @Override
    public List<RequestStatus> findAll() {
        return requestStatusRepository.findAll();
    }
}
