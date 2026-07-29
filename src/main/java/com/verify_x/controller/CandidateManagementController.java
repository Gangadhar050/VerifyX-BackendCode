package com.verify_x.controller;

import com.verify_x.dto.*;
import com.verify_x.services.CandidateManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import org.springframework.web.bind.annotation.RequestParam;



import java.util.List;

@RestController
@RequestMapping("/api/hr/candidates")
@RequiredArgsConstructor
public class CandidateManagementController {

    private final CandidateManagementService candidateManagementService;

    @GetMapping
    public ResponseEntity<PagedResponse<CandidateSummaryDto>> getAllCandidates(

            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) CandidateType candidateType,

            @RequestParam(required = false) ApplicationStatus applicationStatus,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size
    ) {

        return ResponseEntity.ok(
                candidateManagementService.getAllCandidates(
                        keyword,
                        candidateType,
                        applicationStatus,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<CandidateDetailsDto> getCandidateDetails(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                candidateManagementService.getCandidateDetails(candidateId));
    }

    @PostMapping
    public ResponseEntity<CandidateSummaryDto> createCandidate(
            @RequestBody UserRegistrationDto dto
    ) {
        return ResponseEntity.ok(
                candidateManagementService.createCandidate(dto)
        );
    }

    @DeleteMapping("/{candidateId}")
    public ResponseEntity<String> deleteCandidate(
            @PathVariable Long candidateId) {

        candidateManagementService.deleteCandidate(candidateId);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }

    @PutMapping("/{candidateId}/verify-uan")
    public ResponseEntity<String> verifyUan(
            @PathVariable Long candidateId,
            Authentication authentication) {

        candidateManagementService.verifyUan(
                candidateId,
                authentication.getName());

        return ResponseEntity.ok("UAN verified successfully.");
    }

    @PutMapping("/{candidateId}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long candidateId,
            @RequestBody ApplicationStatusUpdateDto dto,
            Authentication authentication) {

        candidateManagementService.updateApplicationStatus(
                candidateId,
                dto,
                authentication.getName());

        return ResponseEntity.ok("Application status updated successfully.");
    }
}
