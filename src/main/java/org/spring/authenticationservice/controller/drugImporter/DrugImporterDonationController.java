package org.spring.authenticationservice.controller.drugImporter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.spring.authenticationservice.DTO.donation.DonationRequestResponseDto;
import org.spring.authenticationservice.DTO.drugImporter.DrugImporterRequestDetailDto;
import org.spring.authenticationservice.Service.drugImporter.impl.DrugImporterRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for drug importers to view donation requests
 */
@RestController
@RequestMapping("/drug-importer/donation-requests")
@PreAuthorize("hasRole('DRUG_IMPORTER')")
@RequiredArgsConstructor
@Tag(name = "Drug Importer Donation Requests", description = "API endpoints for drug importers to view donation requests")
public class DrugImporterDonationController {

    private final DrugImporterRequestService drugImporterRequestService;

    // Helper method to get drug importer ID from authentication
//    private Long getDrugImporterId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() != null) {
//            return ((UserDetailsImpl) authentication.getPrincipal()).getDrugImporterId();
//        }
//        return null; // In production, you might want to handle this better
//    }

    /**
     * Get all pending donation requests with comprehensive details
     * @return List of detailed donation requests
     */
    @Operation(summary = "Get all pending donation requests",
            description = "Returns a list of all  donation requests with detailed information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DrugImporterRequestDetailDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<DrugImporterRequestDetailDto>> getAllDonationRequests() {
//        Long drugImporterId = getDrugImporterId();
        Long drugImporterId = 1L;
        List<DrugImporterRequestDetailDto> requests =
                drugImporterRequestService.getAllDonationRequests(drugImporterId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    /**
     * Get detailed donation request by id with comprehensive information
     * @param requestId The id of the donation request
     * @return Detailed donation request information
     */
    @Operation(summary = "Get donation request by ID",
            description = "Returns detailed information about  donation request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the donation request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DrugImporterRequestDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Donation request not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{requestId}")
    public ResponseEntity<DrugImporterRequestDetailDto> getDonationRequestById(
            @Parameter(description = "ID of the donation request to retrieve", required = true)
            @PathVariable Long requestId) {
        //        Long drugImporterId = getDrugImporterId();
        Long drugImporterId = 1L;
        DrugImporterRequestDetailDto request =
                drugImporterRequestService.getDonationRequestById(requestId, drugImporterId);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }
}