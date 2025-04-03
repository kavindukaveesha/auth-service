package org.spring.authenticationservice.controller.drugImporter;

import org.spring.authenticationservice.DTO.drugImporter.RequestStatusDTO;
import org.spring.authenticationservice.Service.drugImporter.RequestStatusService;
import org.spring.authenticationservice.model.drugImporter.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/request-status")
public class RequestStatusController {

    private final RequestStatusService requestStatusService;

    @Autowired
    public RequestStatusController(RequestStatusService requestStatusService) {
        this.requestStatusService = requestStatusService;
    }

    /**
     * Create a new request status
     * Only DRUG_IMPORTER role can access this endpoint
     */
    @PostMapping
    @PreAuthorize("hasRole('DRUG_IMPORTER')")
    public ResponseEntity<RequestStatusDTO.Response> createRequestStatus(
            @RequestBody RequestStatusDTO.Request requestDTO) {

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestId(requestDTO.getRequestId());
        requestStatus.setDrugImporterId(requestDTO.getDrugImporterId());
        requestStatus.setStatus(requestDTO.getStatus());

        RequestStatus savedRequestStatus = requestStatusService.save(requestStatus);

        RequestStatusDTO.Response response = mapToResponse(savedRequestStatus);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing request status
     * Only DRUG_IMPORTER role can access this endpoint
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DRUG_IMPORTER')")
    public ResponseEntity<RequestStatusDTO.Response> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody RequestStatusDTO.UpdateRequest updateRequest) {

        RequestStatus updatedRequestStatus = requestStatusService.updateStatus(id, updateRequest.getStatus());
        RequestStatusDTO.Response response = mapToResponse(updatedRequestStatus);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestStatusDTO.Response> getRequestStatusById(@PathVariable Long id) {
        return requestStatusService.findById(id)
                .map(requestStatus -> ResponseEntity.ok(mapToResponse(requestStatus)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<RequestStatusDTO.Response>> getRequestStatusByRequestId(
            @PathVariable Long requestId) {

        List<RequestStatus> requestStatuses = requestStatusService.findByRequestId(requestId);
        List<RequestStatusDTO.Response> responses = requestStatuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/drugimporter/{drugImporterId}")
    public ResponseEntity<List<RequestStatusDTO.Response>> getRequestStatusByDrugImporterId(
            @PathVariable Long drugImporterId) {

        List<RequestStatus> requestStatuses = requestStatusService.findByDrugImporterId(drugImporterId);
        List<RequestStatusDTO.Response> responses = requestStatuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/request/{requestId}/drugimporter/{drugImporterId}")
    public ResponseEntity<RequestStatusDTO.Response> getRequestStatusByRequestIdAndDrugImporterId(
            @PathVariable Long requestId,
            @PathVariable Long drugImporterId) {

        return requestStatusService.findByRequestIdAndDrugImporterId(requestId, drugImporterId)
                .map(requestStatus -> ResponseEntity.ok(mapToResponse(requestStatus)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequestStatus(@PathVariable Long id) {
        requestStatusService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RequestStatusDTO.Response>> getAllRequestStatuses() {
        List<RequestStatus> requestStatuses = requestStatusService.findAll();
        List<RequestStatusDTO.Response> responses = requestStatuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private RequestStatusDTO.Response mapToResponse(RequestStatus requestStatus) {
        return new RequestStatusDTO.Response(
                requestStatus.getId(),
                requestStatus.getRequestId(),
                requestStatus.getDrugImporterId(),
                requestStatus.getStatus()
        );
    }
}