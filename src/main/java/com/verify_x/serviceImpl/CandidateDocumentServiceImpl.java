package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Employment;
import com.verify_x.enums.*;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateDocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import java.io.IOException;
import java.util.ArrayList;
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

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Candidate not found"));
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload a document.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Maximum allowed size is 5 MB.");
        }

        if (file.getContentType() == null ||
                !ALLOWED_TYPES.contains(file.getContentType().toLowerCase())) {

            throw new RuntimeException(
                    "Only PDF, PNG, JPG and JPEG files are allowed."
            );
        }
    }

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
                            " candidate."
            );
        }
    }

    private void uploadIfPresent(
            Candidate candidate,
            MultipartFile file,
            DocumentType documentType
    ) {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        validateDocumentType(candidate, documentType);

        if (candidateDocumentRepository.existsByCandidateAndDocumentType(
                candidate,
                documentType
        )) {

            throw new RuntimeException(
                    documentType + " already uploaded."
            );
        }

        try {

            CandidateDocument document = CandidateDocument.builder()
                    .candidate(candidate)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .documentData(file.getBytes())
                    .status(DocumentStatus.PENDING)
                    .rejectionReason(null)
                    .build();

            candidateDocumentRepository.save(document);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to upload " + documentType,
                    e
            );
        }
    }

    @Override
    public void uploadDocuments(
            CandidateDocumentRequest request
    ) {

        Candidate candidate = getLoggedInCandidate();

        uploadIfPresent(candidate,
                request.getResume(),
                DocumentType.RESUME);

        uploadIfPresent(candidate,
                request.getOfferLetter(),
                DocumentType.OFFER_LETTER);

        uploadIfPresent(candidate,
                request.getSalarySlip(),
                DocumentType.SALARY_SLIP);

        uploadIfPresent(candidate,
                request.getRelievingLetter(),
                DocumentType.RELIEVING_LETTER);

        uploadIfPresent(candidate,
                request.getExperienceLetter(),
                DocumentType.EXPERIENCE_LETTER);

        uploadIfPresent(candidate,
                request.getPanCard(),
                DocumentType.PAN_CARD);

        uploadIfPresent(candidate,
                request.getUanProof(),
                DocumentType.UAN_PROOF);

        updateApplicationStatus(candidate);

        log.info("Documents uploaded successfully by {}",
                candidate.getEmail());
    }

    @Override
    public void reUploadDocuments(
            CandidateDocumentRequest request
    ) {

        Candidate candidate = getLoggedInCandidate();

        reUploadIfPresent(candidate,
                request.getResume(),
                DocumentType.RESUME);

        reUploadIfPresent(candidate,
                request.getOfferLetter(),
                DocumentType.OFFER_LETTER);

        reUploadIfPresent(candidate,
                request.getSalarySlip(),
                DocumentType.SALARY_SLIP);

        reUploadIfPresent(candidate,
                request.getRelievingLetter(),
                DocumentType.RELIEVING_LETTER);

        reUploadIfPresent(candidate,
                request.getExperienceLetter(),
                DocumentType.EXPERIENCE_LETTER);

        reUploadIfPresent(candidate,
                request.getPanCard(),
                DocumentType.PAN_CARD);

        reUploadIfPresent(candidate,
                request.getUanProof(),
                DocumentType.UAN_PROOF);

        log.info("Documents re-uploaded successfully by {}",
                candidate.getEmail());
    }

    private void reUploadIfPresent(

            Candidate candidate,

            MultipartFile file,

            DocumentType documentType
    ) {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        validateDocumentType(candidate, documentType);

        CandidateDocument document =
                candidateDocumentRepository
                        .findByCandidateAndDocumentType(
                                candidate,
                                documentType
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        documentType + " not found."
                                ));

        if (document.getStatus() != DocumentStatus.REJECTED) {

            throw new RuntimeException(
                    documentType +
                            " is not rejected. Only rejected documents can be re-uploaded."
            );
        }

        try {

            document.setFileName(file.getOriginalFilename());

            document.setContentType(file.getContentType());

            document.setDocumentData(file.getBytes());

            document.setStatus(DocumentStatus.PENDING);

            document.setRejectionReason(null);

            candidateDocumentRepository.save(document);
            updateApplicationStatus(candidate);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to re-upload " + documentType,
                    e
            );
        }
    }

    @Override
    public List<CandidateDocumentDto> getMyDocuments() {

        Candidate candidate = getLoggedInCandidate();

        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<CandidateDocumentDto> getDocumentsByCandidateId(
            Long candidateId
    ) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found."));

        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CandidateDocument getDocument(Long documentId) {

        return candidateDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found."));
    }

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

        if (document.getStatus() == DocumentStatus.VERIFIED) {

            throw new RuntimeException(
                    "Verified documents cannot be deleted."
            );
        }

        candidateDocumentRepository.delete(document);

        log.info("{} deleted by {}",
                document.getDocumentType(),
                candidate.getEmail());
    }

    @Override
    public Resource downloadDocument(Long documentId) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        if ("CANDIDATE".equals(principal.getRole().name())) {

            if (!document.getCandidate().getId().equals(principal.getUserId())) {

                throw new RuntimeException(
                        "You are not authorized to download this document."
                );
            }
        }

        return new org.springframework.core.io.ByteArrayResource(
                document.getDocumentData()
        );
    }

    @Override
    public void verifyDocument(Long documentId) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found."));

        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new RuntimeException("Document is already verified.");
        }

        document.setStatus(DocumentStatus.VERIFIED);
        document.setRejectionReason(null);

        candidateDocumentRepository.save(document);

        updateApplicationStatus(document.getCandidate());

        log.info("{} verified successfully.",
                document.getDocumentType());
    }

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

        if (document.getStatus() == DocumentStatus.VERIFIED) {

            throw new RuntimeException(
                    "Verified document cannot be rejected."
            );
        }

        document.setStatus(DocumentStatus.REJECTED);
        document.setRejectionReason(rejectionReason);

        candidateDocumentRepository.save(document);

        updateApplicationStatus(document.getCandidate());

        log.info("{} rejected.",
                document.getDocumentType());
    }

    @Override
    public List<CandidateDocumentDto> getPendingDocuments() {

        return candidateDocumentRepository
                .findByStatus(DocumentStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public DashboardStatisticsDto getDashboardStatistics() {

        return DashboardStatisticsDto.builder()
                .totalDocuments(candidateDocumentRepository.count())
                .verified(candidateDocumentRepository.countByStatus(DocumentStatus.VERIFIED))
                .pending(candidateDocumentRepository.countByStatus(DocumentStatus.PENDING))
                .rejected(candidateDocumentRepository.countByStatus(DocumentStatus.REJECTED))
                .build();
    }

    @Override
    public List<CandidateDashboardDto> getCandidateDashboard() {

        List<Candidate> candidates = candidateRepository.findAll();

        List<CandidateDashboardDto> dashboard = new ArrayList<>();

        for (Candidate candidate : candidates) {

            List<CandidateDocument> documents =
                    candidateDocumentRepository.findByCandidate(candidate);

            long verified = documents.stream()
                    .filter(doc -> doc.getStatus() == DocumentStatus.VERIFIED)
                    .count();

            long pending = documents.stream()
                    .filter(doc -> doc.getStatus() == DocumentStatus.PENDING)
                    .count();

            long rejected = documents.stream()
                    .filter(doc -> doc.getStatus() == DocumentStatus.REJECTED)
                    .count();

            dashboard.add(

                    CandidateDashboardDto.builder()

                            .candidateId(candidate.getId())

                            .candidateName(candidate.getUsername())

                            .email(candidate.getEmail())

                            .phoneNumber(candidate.getPhoneNumber())

                            .appliedRole(candidate.getAppliedRole())

                            .candidateType(candidate.getCandidateType())

                            .totalDocuments(documents.size())

                            .verifiedDocuments(verified)

                            .pendingDocuments(pending)

                            .rejectedDocuments(rejected)

                            .build()
            );
        }

        return dashboard;
    }

    @Override
    public void verifyUan(Long candidateId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new RuntimeException("Candidate not found."));

        Employment employment = candidate.getEmployment();

        if (employment == null) {

            throw new RuntimeException(
                    "Employment details not found."
            );
        }

        employment.setUanVerificationStatus(
                VerificationStatus.VERIFIED
        );
    }

    @Override
    public List<HrVerificationRequestDto> getAllVerificationRequests() {

        return candidateRepository.findAll()

                .stream()

                .filter(candidate ->
                        candidateDocumentRepository.countByCandidate(candidate) > 0)

                .map(candidate -> {

                    Long count =
                            candidateDocumentRepository.countByCandidate(candidate);

                    List<CandidateDocument> documents =
                            candidateDocumentRepository.findByCandidate(candidate);

                    DocumentStatus overallStatus = DocumentStatus.PENDING;

                    if (!documents.isEmpty()) {

                        boolean rejected = documents.stream()
                                .anyMatch(doc ->
                                        doc.getStatus() == DocumentStatus.REJECTED);

                        boolean pending = documents.stream()
                                .anyMatch(doc ->
                                        doc.getStatus() == DocumentStatus.PENDING);

                        if (rejected) {

                            overallStatus = DocumentStatus.REJECTED;

                        } else if (pending) {

                            overallStatus = DocumentStatus.PENDING;

                        } else {

                            overallStatus = DocumentStatus.VERIFIED;
                        }
                    }

                    return HrVerificationRequestDto.builder()

                            .candidateId(candidate.getId())

                            .candidateName(candidate.getUsername())

                            .email(candidate.getEmail())

                            .candidateType(candidate.getCandidateType())

                            .documentCount(count.intValue())

                            .uanVerificationStatus(

                                    candidate.getEmployment() != null

                                            ? candidate.getEmployment()
                                            .getUanVerificationStatus()

                                            : VerificationStatus.PENDING
                            )

                            .status(overallStatus)

                            .build();
                })

                .toList();
    }
    private void updateApplicationStatus(Candidate candidate) {

        List<CandidateDocument> documents =
                candidateDocumentRepository.findByCandidate(candidate);

        boolean rejected = documents.stream()
                .anyMatch(d -> d.getStatus() == DocumentStatus.REJECTED);

        boolean pending = documents.stream()
                .anyMatch(d -> d.getStatus() == DocumentStatus.PENDING);

        if (rejected) {
            candidate.setApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED);
        } else if (pending) {
            candidate.setApplicationStatus(ApplicationStatus.PENDING_VERIFICATION);
        } else {
            candidate.setApplicationStatus(ApplicationStatus.DOCUMENTS_VERIFIED);
        }

        candidateRepository.save(candidate);
    }
}

//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.CandidateDashboardDto;
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.DashboardStatisticsDto;
//import com.verify_x.dto.HrVerificationRequestDto;
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.entity.Employment;
//import com.verify_x.enums.CandidateType;
//import com.verify_x.enums.DocumentStatus;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.enums.VerificationStatus;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.jwt.UserPrincipal;
//import com.verify_x.repository.CandidateDocumentRepository;
//import com.verify_x.repository.CandidateRepository;
//import com.verify_x.services.CandidateDocumentService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class CandidateDocumentServiceImpl implements CandidateDocumentService {
//
//    private final CandidateRepository candidateRepository;
//    private final CandidateDocumentRepository candidateDocumentRepository;
//
//    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
//
//    private static final List<String> ALLOWED_TYPES = List.of(
//            "application/pdf",
//            "image/png",
//            "image/jpeg",
//            "image/jpg"
//    );
//
//    private static final Set<DocumentType> FRESHER_DOCUMENTS = Set.of(
//            DocumentType.RESUME,
//            DocumentType.PAN_CARD
//    );
//
//    private static final Set<DocumentType> EXPERIENCED_DOCUMENTS = Set.of(
//            DocumentType.RESUME,
//            DocumentType.OFFER_LETTER,
//            DocumentType.SALARY_SLIP,
//            DocumentType.RELIEVING_LETTER,
//            DocumentType.EXPERIENCE_LETTER,
//            DocumentType.PAN_CARD,
//            DocumentType.UAN_PROOF
//    );
//
//    // ==========================================================
//    // DTO Mapper
//    // ==========================================================
//
//    private CandidateDocumentDto mapToDto(CandidateDocument document) {
//
//        return CandidateDocumentDto.builder()
//                .id(document.getId())
//                .documentType(document.getDocumentType())
//                .fileName(document.getFileName())
//                .contentType(document.getContentType())
//                .status(document.getStatus())
//                .rejectionReason(document.getRejectionReason())
//                .uploadedAt(document.getUploadedAt())
//                .updatedAt(document.getUpdatedAt())
//                .build();
//    }
//
//    // ==========================================================
//    // Logged In Candidate
//    // ==========================================================
//
//    private Candidate getLoggedInCandidate() {
//
//        Authentication authentication =
//                SecurityContextHolder.getContext().getAuthentication();
//
//        UserPrincipal principal =
//                (UserPrincipal) authentication.getPrincipal();
//
//        return candidateRepository.findById(principal.getUserId())
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("Candidate not found"));
//    }
//
//    // ==========================================================
//    // Validate File
//    // ==========================================================
//
//    private void validateFile(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//            throw new RuntimeException("Please upload a document.");
//        }
//
//        if (file.getSize() > MAX_FILE_SIZE) {
//            throw new RuntimeException("Maximum file size is 5 MB.");
//        }
//
//        if (!ALLOWED_TYPES.contains(file.getContentType())) {
//            throw new RuntimeException(
//                    "Only PDF, PNG, JPG and JPEG files are allowed."
//            );
//        }
//    }
//
//    // ==========================================================
//    // Validate Candidate Document Type
//    // ==========================================================
//
//    private void validateDocumentType(
//            Candidate candidate,
//            DocumentType documentType
//    ) {
//
//        Set<DocumentType> allowedDocuments =
//                candidate.getCandidateType() == CandidateType.FRESHER
//                        ? FRESHER_DOCUMENTS
//                        : EXPERIENCED_DOCUMENTS;
//
//        if (!allowedDocuments.contains(documentType)) {
//
//            throw new RuntimeException(
//                    documentType +
//                            " is not allowed for " +
//                            candidate.getCandidateType() +
//                            " candidates."
//            );
//        }
//    }
//
//    // ==========================================================
//    // Upload Document
//    // ==========================================================
//
//    @Override
//    public void uploadDocument(
//            MultipartFile file,
//            DocumentType documentType
//    ) {
//
//        try {
//
//            validateFile(file);
//
//            Candidate candidate = getLoggedInCandidate();
//
//            validateDocumentType(candidate, documentType);
//
//            CandidateDocument existing =
//                    candidateDocumentRepository
//                            .findByCandidateAndDocumentType(
//                                    candidate,
//                                    documentType
//                            )
//                            .orElse(null);
//
//            if (existing != null) {
//
//                throw new RuntimeException(
//                        documentType +
//                                " already uploaded. Use Re-Upload if rejected."
//                );
//            }
//
//            CandidateDocument document =
//                    CandidateDocument.builder()
//
//                            .candidate(candidate)
//
//                            .documentType(documentType)
//
//                            .fileName(file.getOriginalFilename())
//
//                            .contentType(file.getContentType())
//
//                            .documentData(file.getBytes())
//
//                            .status(DocumentStatus.PENDING)
//
//                            .rejectionReason(null)
//
//                            .build();
//
//            candidateDocumentRepository.save(document);
//
//            log.info("{} uploaded by {}",
//                    documentType,
//                    candidate.getEmail());
//
//        } catch (IOException ex) {
//
//            log.error("File upload failed", ex);
//
//            throw new RuntimeException(
//                    "Unable to upload document."
//            );
//        }
//    }
//
//    // ==========================================================
//    // Re Upload
//    // ==========================================================
//
//    @Override
//    public void reUploadDocument(
//            MultipartFile file,
//            DocumentType documentType
//    ) {
//
//        try {
//
//            validateFile(file);
//
//            Candidate candidate = getLoggedInCandidate();
//
//            validateDocumentType(candidate, documentType);
//
//            CandidateDocument document =
//                    candidateDocumentRepository
//                            .findByCandidateAndDocumentType(
//                                    candidate,
//                                    documentType
//                            )
//                            .orElseThrow(() ->
//                                    new RuntimeException(
//                                            documentType +
//                                                    " not found."
//                                    ));
//
//            if (document.getStatus() != DocumentStatus.REJECTED) {
//
//                throw new RuntimeException(
//                        "Only rejected documents can be re-uploaded."
//                );
//            }
//
//            document.setFileName(file.getOriginalFilename());
//
//            document.setContentType(file.getContentType());
//
//            document.setDocumentData(file.getBytes());
//
//            document.setStatus(DocumentStatus.PENDING);
//
//            document.setRejectionReason(null);
//
//            candidateDocumentRepository.save(document);
//
//            log.info("{} re-uploaded by {}",
//                    documentType,
//                    candidate.getEmail());
//
//        } catch (IOException ex) {
//
//            log.error("Document re-upload failed", ex);
//
//            throw new RuntimeException(
//                    "Unable to re-upload document."
//            );
//        }
//    }
//    // ==========================================================
//    // Get Logged-In Candidate Documents
//    // ==========================================================
//
//    @Override
//    public List<CandidateDocumentDto> getMyDocuments() {
//
//        Candidate candidate = getLoggedInCandidate();
//
//        return candidateDocumentRepository
//                .findByCandidate(candidate)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    // ==========================================================
//    // Get Documents By Candidate Id (HR/Admin)
//    // ==========================================================
//
//    @Override
//    public List<CandidateDocumentDto> getDocumentsByCandidateId(
//            Long candidateId
//    ) {
//
//        Candidate candidate = candidateRepository
//                .findById(candidateId)
//                .orElseThrow(() ->
//                        new RuntimeException("Candidate not found."));
//
//        return candidateDocumentRepository
//                .findByCandidate(candidate)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    // ==========================================================
//    // View Document
//    // ==========================================================
//    @Override
//    public CandidateDocument getDocument(Long documentId) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new RuntimeException("Document not found."));
//
//        return document;
//    }
//
//    // ==========================================================
//    // Delete Document
//    // ==========================================================
//
//    @Override
//    public void deleteDocument(Long documentId) {
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new RuntimeException("Document not found."));
//
//        if (!document.getCandidate().getId().equals(candidate.getId())) {
//            throw new RuntimeException(
//                    "You are not allowed to delete this document."
//            );
//        }
//     // Improvement: Prevent deleting verified documents
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//            throw new RuntimeException(
//                    "Verified documents cannot be deleted."
//            );
//        }
//        candidateDocumentRepository.delete(document);
//
//        log.info("{} deleted by {}",
//                document.getDocumentType(),
//                candidate.getEmail());
//    }
//
//    // ==========================================================
//    // Verify Document (HR/Admin)
//    // ==========================================================
//
//    @Override
//    public void verifyDocument(Long documentId) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new RuntimeException("Document not found."));
//
//        // Prevent verifying again
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//            throw new RuntimeException("Document is already verified.");
//        }
//
//        document.setStatus(DocumentStatus.VERIFIED);
//        document.setRejectionReason(null);
//        candidateDocumentRepository.save(document);
//
//        log.info("{} verified successfully.",
//                document.getDocumentType());
//    }
//
//    // ==========================================================
//    // Reject Document (HR/Admin)
//    // ==========================================================
//
//    @Override
//    public void rejectDocument(
//            Long documentId,
//            String rejectionReason
//    ) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new RuntimeException("Document not found."));
//
//        // Prevent rejecting a verified document
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//            throw new RuntimeException(
//                    "Verified document cannot be rejected."
//            );
//        }
//
//        document.setStatus(DocumentStatus.REJECTED);
//        document.setRejectionReason(rejectionReason);
//        candidateDocumentRepository.save(document);
//
//        log.info("{} rejected.",
//                document.getDocumentType());
//    }
//
//    // Fetch all pending documents for HR review.
//    @Override
//    public List<CandidateDocumentDto> getPendingDocuments() {
//
//        return candidateDocumentRepository
//                .findByStatus(DocumentStatus.PENDING)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    @Override
//    public Resource downloadDocument(Long documentId) {
//
//        CandidateDocument document = candidateDocumentRepository
//                .findById(documentId)
//                .orElseThrow(() ->
//                        new RuntimeException("Document not found."));
//
//        Authentication authentication =
//                SecurityContextHolder.getContext().getAuthentication();
//
//        UserPrincipal principal =
//                (UserPrincipal) authentication.getPrincipal();
//
//        // Candidate can access only their own documents
//        if ("CANDIDATE".equals(principal.getRole().name())) {
//
//            if (!document.getCandidate().getId().equals(principal.getUserId())) {
//
//                throw new RuntimeException(
//                        "You are not authorized to access this document.");
//
//            }
//        }
//
//        try {
//
//
//            Path path = Paths.get(Arrays.toString(document.getDocumentData()));
//
//            Resource resource = new UrlResource(path.toUri());
//
//            if (!resource.exists() || !resource.isReadable()) {
//                throw new RuntimeException("File not found.");
//            }
//
//            return resource;
//
//        } catch (Exception e) {
//
//            log.error("Unable to download document.", e);
//
//            throw new RuntimeException("Unable to download document.");
//        }
//    }
//    @Override
//    public DashboardStatisticsDto getDashboardStatistics() {
//        return DashboardStatisticsDto.builder()
//                .totalDocuments(candidateDocumentRepository.count())
//                .verified(candidateDocumentRepository.countByStatus(DocumentStatus.VERIFIED))
//                .pending(candidateDocumentRepository.countByStatus(DocumentStatus.PENDING))
//                .rejected(candidateDocumentRepository.countByStatus(DocumentStatus.REJECTED))
//                .build();
//    }
//    @Override
//    public List<CandidateDashboardDto> getCandidateDashboard() {
//
//        List<Candidate> candidates = candidateRepository.findAll();
//
//        if (candidates.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        List<CandidateDashboardDto> dashboardList = new ArrayList<>();
//
//        for (Candidate candidate : candidates) {
//
//            List<CandidateDocument> documents =
//                    candidateDocumentRepository.findByCandidateId(candidate.getId());
//
//            long verified = documents.stream()
//                    .filter(document -> document.getStatus() == DocumentStatus.VERIFIED)
//                    .count();
//
//            long pending = documents.stream()
//                    .filter(document -> document.getStatus() == DocumentStatus.PENDING)
//                    .count();
//
//            long rejected = documents.stream()
//                    .filter(document -> document.getStatus() == DocumentStatus.REJECTED)
//                    .count();
//
//            dashboardList.add(
//                    CandidateDashboardDto.builder()
//                            .candidateId(candidate.getId())
//                            .candidateName(candidate.getUsername())
//                            .email(candidate.getEmail())
//                            .phoneNumber(candidate.getPhoneNumber())
//                            .appliedRole(candidate.getAppliedRole())
//                            .candidateType(candidate.getCandidateType())
//                            .totalDocuments(documents.size())
//                            .verifiedDocuments(verified)
//                            .pendingDocuments(pending)
//                            .rejectedDocuments(rejected)
//                            .build()
//            );
//        }
//
//        return dashboardList;
//    }
//
//    @Override
//    public void verifyUan(Long candidateId) {
//
//        Candidate candidate = candidateRepository.findById(candidateId)
//                .orElseThrow(() -> new RuntimeException("Candidate not found."));
//
//        Employment employment = candidate.getEmployment();
//
//        if (employment == null) {
//            throw new RuntimeException("Employment details not found.");
//        }
//
//        employment.setUanVerificationStatus(VerificationStatus.VERIFIED);
//    }
//
//    @Override
//    public List<HrVerificationRequestDto> getAllVerificationRequests() {
//
//        return candidateRepository.findAll()
//                .stream()
//                .filter(candidate ->
//                        candidateDocumentRepository.countByCandidate(candidate) > 0)
//                .map(candidate -> {
//
//                    Long documentCount =
//                            candidateDocumentRepository.countByCandidate(candidate);
//
//                    List<CandidateDocument> documents =
//                            candidateDocumentRepository.findByCandidate(candidate);
//
//                    DocumentStatus overallStatus = DocumentStatus.PENDING;
//
//                    if (!documents.isEmpty()) {
//
//                        boolean rejected = documents.stream()
//                                .anyMatch(doc -> doc.getStatus() == DocumentStatus.REJECTED);
//
//                        boolean pending = documents.stream()
//                                .anyMatch(doc -> doc.getStatus() == DocumentStatus.PENDING);
//
//                        if (rejected) {
//                            overallStatus = DocumentStatus.REJECTED;
//                        } else if (pending) {
//                            overallStatus = DocumentStatus.PENDING;
//                        } else {
//                            overallStatus = DocumentStatus.VERIFIED;
//                        }
//                    }
//
//                    return HrVerificationRequestDto.builder()
//                            .candidateId(candidate.getId())
//                            .candidateName(candidate.getUsername())
//                            .email(candidate.getEmail())
//                            .candidateType(candidate.getCandidateType())
//                            .documentCount(documentCount.intValue())
//                            .uanVerificationStatus(
//                                    candidate.getEmployment() != null
//                                            ? candidate.getEmployment().getUanVerificationStatus()
//                                            : VerificationStatus.PENDING
//                            )
//                            .status(overallStatus)
//                            .build();
//                })
//                .toList();
//    }
//}