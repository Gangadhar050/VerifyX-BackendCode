package com.verify_x.services;

import com.verify_x.dto.CandidateDashboardDto;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.DashboardStatisticsDto;
import com.verify_x.enums.DocumentType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateDocumentService {

    // Candidate uploads a document.
    void uploadDocument(
            MultipartFile file,
            DocumentType documentType
    );

    // Candidate re-uploads a rejected document.
    void reUploadDocument(
            MultipartFile file,
            DocumentType documentType
    );

    // Get logged-in candidate documents.
    List<CandidateDocumentDto> getMyDocuments();

    // HR/Admin view candidate documents.
    List<CandidateDocumentDto> getDocumentsByCandidateId(Long candidateId);

    // Get all pending documents.
    List<CandidateDocumentDto> getPendingDocuments();

    // Get document details.
    CandidateDocumentDto getDocument(Long documentId);

    // Candidate deletes a document.
    void deleteDocument(Long documentId);

    // HR verifies document.
    void verifyDocument(Long documentId);

    // HR rejects document.
    void rejectDocument(
            Long documentId,
            String rejectionReason
    );

    // Download candidate document.
    Resource downloadDocument(Long documentId);

    // ===============================
    // HR Dashboard Statistics
    // ===============================
    DashboardStatisticsDto getDashboardStatistics();
    
 // HR Dashboard
    List<CandidateDashboardDto> getCandidateDashboard();
}