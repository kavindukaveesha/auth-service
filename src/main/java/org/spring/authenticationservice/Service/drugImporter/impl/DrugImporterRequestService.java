package org.spring.authenticationservice.Service.drugImporter.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.authenticationservice.DTO.donation.DonationRequestResponseDto;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRequestDetailDto;
import org.spring.authenticationservice.DTO.patient.PatientResponseDto;
import org.spring.authenticationservice.Service.drugImporter.impl.RequestStatusServiceImpl;
import org.spring.authenticationservice.Service.patient.DonationRequestService;
import org.spring.authenticationservice.Service.patient.PatientService;
import org.spring.authenticationservice.mapper.patient.PatientMapper;
import org.spring.authenticationservice.model.drugImporter.RequestStatus;
import org.spring.authenticationservice.model.patient.Patient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling drug importer donation request operations
 * Aggregates data from multiple services to provide comprehensive information
 */
@Service
@RequiredArgsConstructor
public class DrugImporterRequestService {

    private final DonationRequestService donationRequestService;
    private final PatientService patientService;
    private final RequestStatusServiceImpl requestStatusService;
    private final PatientMapper patientMapper;

    /**
     * Get all pending donation requests with detailed information
     * including patient details and request status
     *
     * @param drugImporterId The ID of the drug importer
     * @return List of detailed donation request DTOs
     */
    public List<DrugImporterRequestDetailDto> getAllDonationRequests(Long drugImporterId) {
        // Get all pending donation requests
        List<DonationRequestResponseDto> pendingRequests = donationRequestService.getPendingDonationRequests();

        // Convert to detailed DTOs with additional information
        return pendingRequests.stream()
                .map(request -> convertToDrugImporterRequestDetail(request, drugImporterId))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific donation request with detailed information
     * including patient details and request status
     *
     * @param requestId The ID of the donation request
     * @param drugImporterId The ID of the drug importer
     * @return Detailed donation request DTO
     */
    public DrugImporterRequestDetailDto getDonationRequestById(Long requestId, Long drugImporterId) {
        // Get all pending donation requests
        List<DonationRequestResponseDto> allRequests = donationRequestService.getPendingDonationRequests();

        // Find the specific request
        DonationRequestResponseDto request = allRequests.stream()
                .filter(req -> req.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Donation request not found with id: " + requestId));

        // Convert to detailed DTO with additional information
        return convertToDrugImporterRequestDetail(request, drugImporterId);
    }

    /**
     * Convert a donation request to a detailed DTO with patient and status information
     *
     * @param request The donation request DTO
     * @param drugImporterId The ID of the drug importer
     * @return Detailed donation request DTO
     */
    private DrugImporterRequestDetailDto convertToDrugImporterRequestDetail(
            DonationRequestResponseDto request, Long drugImporterId) {

        // Get patient information
        Patient patient = patientService.getPatientById(request.getPatientId());
        PatientResponseDto patientDto = patientMapper.toResponseDto(patient);

        // Get request status information - FIXED to handle multiple results
        List<RequestStatus> requestStatuses =
                requestStatusService.findByRequestId(request.getRequestId())
                        .stream()
                        .filter(status -> status.getDrugImporterId().equals(drugImporterId))
                        .collect(Collectors.toList());

        // Use the most recent status if there are multiple
        RequestStatus currentStatus = null;
        if (!requestStatuses.isEmpty()) {
            // You might want to sort by creation date/ID if you have that field
            // For now, we'll just take the first one
            currentStatus = requestStatuses.get(0);
        }

        // Build the detailed DTO
        return DrugImporterRequestDetailDto.builder()
                .donationRequest(request)
                .patient(patientDto)
                .requestStatus(currentStatus)
                .build();
    }
}