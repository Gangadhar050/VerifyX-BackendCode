package com.verify_x.services;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.HrVerificationRequestDto;
import com.verify_x.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateDocumentService {

    // Candidate uploads document
    void uploadDocument(
            MultipartFile file,
            DocumentType documentType
    );

    // Candidate re-uploads rejected document
    void reUploadDocument(
            MultipartFile file,
            DocumentType documentType
    );

    // Logged-in candidate documents
    List<CandidateDocumentDto> getMyDocuments();

    // HR/Admin view candidate documents
    List<CandidateDocumentDto> getDocumentsByCandidateId(
            Long candidateId
    );

    // HR Verification Requests
    List<HrVerificationRequestDto> getAllVerificationRequests();

    // Download document
    CandidateDocumentDto getDocument(
            Long documentId
    );

    // Delete document
    void deleteDocument(
            Long documentId
    );

    // Verify document
    void verifyDocument(
            Long documentId
    );
    //verify uan number
    void verifyUan(
    		Long candidateId);

    // Reject document
    void rejectDocument(
            Long documentId,
            String rejectionReason
    );

}