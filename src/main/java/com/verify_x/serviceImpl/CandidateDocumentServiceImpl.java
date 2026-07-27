package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateDocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateDocumentServiceImpl implements CandidateDocumentService {

    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final Set<DocumentType> FRESHER_DOCUMENTS = Set.of(
            DocumentType.RESUME,
            DocumentType.PAN_CARD
    );

    private static final Set<DocumentType> EXPERIENCED_DOCUMENTS = Set.of(
            DocumentType.RESUME,
            DocumentType.OFFER_LETTER,
            DocumentType.SALARY_SLIP,
            DocumentType.RELIEVING_LETTER,
            DocumentType.EXPERIENCE_LETTER,
            DocumentType.PAN_CARD,
            DocumentType.UAN_PROOF
    );

    // ==========================================================
    // DTO Mapper
    // ==========================================================

    private CandidateDocumentDto mapToDto(CandidateDocument document) {

        return CandidateDocumentDto.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    // ==========================================================
    // Logged In Candidate
    // ==========================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Candidate not found"));
    }

    // ==========================================================
    // Validate File
    // ==========================================================

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload a document.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Maximum file size is 5 MB.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException(
                    "Only PDF, PNG, JPG and JPEG files are allowed."
            );
        }
    }

    // ==========================================================
    // Validate Candidate Document Type
    // ==========================================================

    private void validateDocumentType(
            Candidate candidate,
            DocumentType documentType
    ) {

        Set<DocumentType> allowedDocuments =
                candidate.getCandidateType() == CandidateType.FRESHER
                        ? FRESHER_DOCUMENTS
                        : EXPERIENCED_DOCUMENTS;

        if (!allowedDocuments.contains(documentType)) {

            throw new RuntimeException(
                    documentType +
                            " is not allowed for " +
                            candidate.getCandidateType() +
                            " candidates."
            );
        }
    }

    // ==========================================================
    // Upload Document
    // ==========================================================

    @Override
    public void uploadDocument(
            MultipartFile file,
            DocumentType documentType
    ) {

        try {

            validateFile(file);

            Candidate candidate = getLoggedInCandidate();

            validateDocumentType(candidate, documentType);

            CandidateDocument existing =
                    candidateDocumentRepository
                            .findByCandidateAndDocumentType(
                                    candidate,
                                    documentType
                            )
                            .orElse(null);

            if (existing != null) {

                throw new RuntimeException(
                        documentType +
                                " already uploaded. Use Re-Upload if rejected."
                );
            }

            CandidateDocument document =
                    CandidateDocument.builder()

                            .candidate(candidate)

                            .documentType(documentType)

                            .fileName(file.getOriginalFilename())

                            .contentType(file.getContentType())

                            .documentData(file.getBytes())

                            .status(DocumentStatus.PENDING)

                            .rejectionReason(null)

                            .build();

            candidateDocumentRepository.save(document);

            log.info("{} uploaded by {}",
                    documentType,
                    candidate.getEmail());

        } catch (IOException ex) {

            log.error("File upload failed", ex);

            throw new RuntimeException(
                    "Unable to upload document."
            );
        }
    }

    // ==========================================================
    // Re Upload
    // ==========================================================

    @Override
    public void reUploadDocument(
            MultipartFile file,
            DocumentType documentType
    ) {

        try {

            validateFile(file);

            Candidate candidate = getLoggedInCandidate();

            validateDocumentType(candidate, documentType);

            CandidateDocument document =
                    candidateDocumentRepository
                            .findByCandidateAndDocumentType(
                                    candidate,
                                    documentType
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            documentType +
                                                    " not found."
                                    ));

            if (document.getStatus() != DocumentStatus.REJECTED) {

                throw new RuntimeException(
                        "Only rejected documents can be re-uploaded."
                );
            }

            document.setFileName(file.getOriginalFilename());

            document.setContentType(file.getContentType());

            document.setDocumentData(file.getBytes());

            document.setStatus(DocumentStatus.PENDING);

            document.setRejectionReason(null);

            candidateDocumentRepository.save(document);

            log.info("{} re-uploaded by {}",
                    documentType,
                    candidate.getEmail());

        } catch (IOException ex) {

            log.error("Document re-upload failed", ex);

            throw new RuntimeException(
                    "Unable to re-upload document."
            );
        }
    }
    // ==========================================================
    // Get Logged-In Candidate Documents
    // ==========================================================

    @Override
    public List<CandidateDocumentDto> getMyDocuments() {

        Candidate candidate = getLoggedInCandidate();

        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ==========================================================
    // Get Documents By Candidate Id (HR/Admin)
    // ==========================================================

    @Override
    public List<CandidateDocumentDto> getDocumentsByCandidateId(
            Long candidateId
    ) {

        Candidate candidate = candidateRepository
                .findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found."));

        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ==========================================================
    // View Document
    // ==========================================================
    @Override
    public CandidateDocument getDocument(Long documentId) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        return document;
    }

    // ==========================================================
    // Delete Document
    // ==========================================================

    @Override
    public void deleteDocument(Long documentId) {

        Candidate candidate = getLoggedInCandidate();

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        if (!document.getCandidate().getId().equals(candidate.getId())) {
            throw new RuntimeException(
                    "You are not allowed to delete this document."
            );
        }

        candidateDocumentRepository.delete(document);

        log.info("{} deleted by {}",
                document.getDocumentType(),
                candidate.getEmail());
    }

    // ==========================================================
    // Verify Document (HR/Admin)
    // ==========================================================

    @Override
    public void verifyDocument(Long documentId) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        document.setStatus(DocumentStatus.VERIFIED);

        document.setRejectionReason(null);

        candidateDocumentRepository.save(document);

        log.info("{} verified successfully.",
                document.getDocumentType());
    }

    // ==========================================================
    // Reject Document (HR/Admin)
    // ==========================================================

    @Override
    public void rejectDocument(
            Long documentId,
            String rejectionReason
    ) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        document.setStatus(DocumentStatus.REJECTED);

        document.setRejectionReason(rejectionReason);

        candidateDocumentRepository.save(document);

        log.info("{} rejected.",
                document.getDocumentType());
    }

}