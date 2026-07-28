package com.verify_x.controller;

import com.verify_x.dto.CandidateDashboardDto;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.DashboardStatisticsDto;
import com.verify_x.enums.DocumentType;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentService candidateDocumentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentType documentType) {

        candidateDocumentService.uploadDocument(file, documentType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully.", null));
    }

    @PutMapping(value = "/re-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> reUploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentType documentType) {

        candidateDocumentService.reUploadDocument(file, documentType);

        return ResponseEntity.ok(
                ApiResponse.success("Document re-uploaded successfully.", null));
    }

    @GetMapping("/my-documents")
    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getMyDocuments() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Documents fetched successfully.",
                        candidateDocumentService.getMyDocuments()));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getCandidateDocuments(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Documents fetched successfully.",
                        candidateDocumentService.getDocumentsByCandidateId(candidateId)));
    }

    // ==============================
    // Dashboard Statistics
    // ==============================
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsDto>> getDashboardStatistics() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard statistics fetched successfully.",
                        candidateDocumentService.getDashboardStatistics()));
    }
    @GetMapping("/view/{documentId}")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId) {

        Resource resource = candidateDocumentService.downloadDocument(documentId);

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId) {

        Resource resource = candidateDocumentService.downloadDocument(documentId);

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<CandidateDashboardDto>>> getCandidateDashboard() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Candidate dashboard fetched successfully.",
                        candidateDocumentService.getCandidateDashboard()
                )
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<CandidateDocumentDto>> getDocument(
            @PathVariable Long documentId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document fetched successfully.",
                        candidateDocumentService.getDocument(documentId)));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @PathVariable Long documentId) {

        candidateDocumentService.deleteDocument(documentId);
        
        

        return ResponseEntity.ok(
                ApiResponse.success("Document deleted successfully.", null));
    }

    @PutMapping("/verify/{documentId}")
    public ResponseEntity<ApiResponse<String>> verifyDocument(
            @PathVariable Long documentId) {

        candidateDocumentService.verifyDocument(documentId);

        return ResponseEntity.ok(
                ApiResponse.success("Document verified successfully.", null));
    }

    @PutMapping("/reject/{documentId}")
    public ResponseEntity<ApiResponse<String>> rejectDocument(
            @PathVariable Long documentId,
            @RequestParam String rejectionReason) {

        candidateDocumentService.rejectDocument(documentId, rejectionReason);

        return ResponseEntity.ok(
                ApiResponse.success("Document rejected successfully.", null));
    }
}