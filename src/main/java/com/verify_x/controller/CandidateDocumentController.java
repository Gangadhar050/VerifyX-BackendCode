package com.verify_x.controller;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.DocumentType;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentService candidateDocumentService;

    // ==========================================================
    // Upload Document (Candidate)
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentType documentType) {

        candidateDocumentService.uploadDocument(file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully.", null));

    }

    // ==========================================================
    // Re Upload
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @PutMapping(
            value = "/re-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> reUploadDocument(

            @RequestParam("file") MultipartFile file,

            @RequestParam DocumentType documentType

    ) {

        candidateDocumentService.reUploadDocument(file, documentType);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document re-uploaded successfully.",
                        (CandidateProfileDto) null
                )
        );
    }

    // ==========================================================
    // Logged In Candidate Documents
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/my-documents")
    public ResponseEntity<List<CandidateDocumentDto>> getMyDocuments() {

        return ResponseEntity.ok(
                candidateDocumentService.getMyDocuments());

    }

    // ==========================================================
    // HR/Admin View Candidate Documents
    // ==========================================================

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getCandidateDocuments(

            @PathVariable Long candidateId

    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched successfully.",
                        (CandidateProfileDto) candidateDocumentService.getDocumentsByCandidateId(candidateId)
                )
        );
    }

    // ==========================================================
    // View Document
    // ==========================================================

    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
    @GetMapping("/view/{documentId}")
    public ResponseEntity<byte[]> viewDocument(
            @PathVariable Long documentId
    ) {

        CandidateDocument document =
                candidateDocumentService.getDocument(documentId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFileName() + "\""
                )
                .contentType(
                        MediaType.parseMediaType(document.getContentType())
                )
                .body(document.getDocumentData());
    }

    // ==========================================================
    // Delete Document
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(

            @PathVariable Long documentId

    ) {

        candidateDocumentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document deleted successfully.",
                        (CandidateProfileDto) null
                )
        );
    }

    // ==========================================================
    // Verify Document (HR/Admin)
    // ==========================================================

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PutMapping("/verify/{documentId}")
    public ResponseEntity<ApiResponse<String>> verifyDocument(

            @PathVariable Long documentId

    ) {

        candidateDocumentService.verifyDocument(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document verified successfully.",
                        (CandidateProfileDto) null
                )
        );
    }

    // ==========================================================
    // Reject Document (HR/Admin)
    // ==========================================================

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PutMapping("/reject/{documentId}")
    public ResponseEntity<ApiResponse<String>> rejectDocument(

            @PathVariable Long documentId,

            @RequestParam String rejectionReason

    ) {

        candidateDocumentService.rejectDocument(
                documentId,
                rejectionReason
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document rejected successfully.",
                        (CandidateProfileDto) null
                )
        );
    }

//    @PutMapping("/verify-uan/{candidateId}")
//    public ResponseEntity<ApiResponse<String>> verifyUan(
//            @PathVariable Long candidateId) {
//
//        candidateDocumentService.verifyUan(candidateId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "UAN verified successfully.",
//                        null
//                )
//        );
//    }
}