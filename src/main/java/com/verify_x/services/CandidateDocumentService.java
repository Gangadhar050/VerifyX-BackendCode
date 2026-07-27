package com.verify_x.services;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.DocumentType;
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

    // Download candidate document.
//    CandidateDocumentDto getDocument(Long documentId);
  CandidateDocument getDocument(Long documentId);
    // Candidate deletes a document.
    void deleteDocument(Long documentId);

    // HR verifies document.
    void verifyDocument(Long documentId);

    /**
     * HR rejects document.
     */
    void rejectDocument(
            Long documentId,
            String rejectionReason
    );

}