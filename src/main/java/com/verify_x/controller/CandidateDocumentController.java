package com.verify_x.controller;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.enums.DocumentType;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentService candidateDocumentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadDocument(

            @RequestParam MultipartFile file,

            @RequestParam DocumentType documentType

    ) {

        candidateDocumentService.uploadDocument(file, documentType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Document uploaded successfully.",
                        (CandidateProfileDto) null
                ));
    }

    @PutMapping("/re-upload")
    public ResponseEntity<ApiResponse<String>> reUploadDocument(

            @RequestParam MultipartFile file,

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

    @GetMapping("/my-documents")
    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getMyDocuments() {

        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        "Documents fetched successfully.",

                        (CandidateProfileDto) candidateDocumentService.getMyDocuments()
                )
        );
    }

    // Get Candidate Documents (HR/Admin)
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getCandidateDocuments(

            @PathVariable Long candidateId

    ) {

        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        "Documents fetched successfully.",

                        (CandidateProfileDto) candidateDocumentService
                                .getDocumentsByCandidateId(candidateId)
                )
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<CandidateDocumentDto>> getDocument(

            @PathVariable Long documentId

    ) {

        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        "Document fetched successfully.",

                        candidateDocumentService.getDocument(documentId)
                )
        );
    }

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

    // Verify Document (HR/Admin)
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

// Reject Document (HR/Admin)
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
}

