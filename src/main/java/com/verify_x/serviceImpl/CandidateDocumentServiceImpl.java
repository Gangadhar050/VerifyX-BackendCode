

package com.verify_x.serviceImpl;


import com.verify_x.dto.CandidateDashboardDto;
import java.util.ArrayList;
import com.verify_x.dto.DashboardStatisticsDto;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateDocumentServiceImpl implements CandidateDocumentService {

    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;

    // Upload Folder
    private static final String UPLOAD_DIR = "uploads/documents/";

    // File Size
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Allowed File Types
    private static final List<String> ALLOWED_TYPES =
            List.of(

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

    // DTO Mapper
    private CandidateDocumentDto mapToDto(CandidateDocument document) {

        return CandidateDocumentDto.builder()

                .id(document.getId())

                .documentType(document.getDocumentType())

                .fileName(document.getFileName())

                .filePath(document.getFilePath())

                .status(document.getStatus())

                .rejectionReason(document.getRejectionReason())

                .uploadedAt(document.getUploadedAt())

                .updatedAt(document.getUpdatedAt())

                .build();

    }

    //LoggedIn-Candidate
    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return candidateRepository.findById(
                        principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Candidate not found"));

    }

    // Validate File
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Please upload a document.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new RuntimeException(
                    "Maximum file size is 5 MB.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {

            throw new RuntimeException(
                    "Only PDF, PNG, JPG and JPEG files are allowed."
            );
        }
    }


    // Create Upload Folder
    private void createUploadDirectory()
            throws IOException {

        Path path = Paths.get(UPLOAD_DIR);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    // Save File
    private String saveFile(MultipartFile file) throws IOException {

        createUploadDirectory();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + file.getOriginalFilename();
        Path path =
                Paths.get(UPLOAD_DIR, fileName);

        Files.copy(
                file.getInputStream(),
                path,
                StandardCopyOption.REPLACE_EXISTING);
        return path.toString();

    }

    @Override
    public void uploadDocument(MultipartFile file, DocumentType documentType) {

        try {

            // Validate uploaded file
            validateFile(file);

            // Logged-in candidate
            Candidate candidate = getLoggedInCandidate();

            // Validate document type based on candidate type
            Set<DocumentType> allowedDocuments =
                    candidate.getCandidateType() == CandidateType.FRESHER
                            ? FRESHER_DOCUMENTS
                            : EXPERIENCED_DOCUMENTS;

            if (!allowedDocuments.contains(documentType)) {

                throw new RuntimeException(
                        documentType + " is not allowed for "
                                + candidate.getCandidateType()
                                + " candidates."
                );

            }
            // Check whether the document already exists
            CandidateDocument existingDocument =
                    candidateDocumentRepository
                            .findByCandidateAndDocumentType(candidate, documentType)
                            .orElse(null);

            if (existingDocument != null) {

                throw new RuntimeException(
                        documentType + " already uploaded. " +
                                "Use Re-Upload if HR rejected this document."
                );

            }

            // Save file to local storage
            String savedPath = saveFile(file);

            // Create new document
            CandidateDocument document = CandidateDocument.builder()

                    .candidate(candidate)

                    .documentType(documentType)

                    .fileName(file.getOriginalFilename())

                    .filePath(savedPath)

                    .status(DocumentStatus.PENDING)

                    .rejectionReason(null)

                    .uploadedAt(LocalDateTime.now())

                    .updatedAt(LocalDateTime.now())

                    .build();

            candidateDocumentRepository.save(document);

            log.info(
                    "{} uploaded successfully by {}",
                    documentType,
                    candidate.getEmail()
            );

        } catch (IOException e) {

            log.error("File upload failed", e);

            throw new RuntimeException(
                    "Unable to upload document."
            );

        }

    }

    @Override
    public void reUploadDocument(
            MultipartFile file,
            DocumentType documentType
    ) {

        try {

            // Validate uploaded file
            validateFile(file);

            // Logged-in candidate
            Candidate candidate = getLoggedInCandidate();
            // Validate document type based on candidate type
            Set<DocumentType> allowedDocuments =
                    candidate.getCandidateType() == CandidateType.FRESHER
                            ? FRESHER_DOCUMENTS
                            : EXPERIENCED_DOCUMENTS;

            if (!allowedDocuments.contains(documentType)) {

                throw new RuntimeException(
                        documentType + " is not allowed for "
                                + candidate.getCandidateType()
                                + " candidates."
                );

            }


            // Find existing document
            CandidateDocument document =
                    candidateDocumentRepository
                            .findByCandidateAndDocumentType(candidate, documentType)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            documentType + " not found."
                                    ));

            // Only rejected documents can be re-uploaded
            if (document.getStatus() != DocumentStatus.REJECTED) {

                throw new RuntimeException(
                        "Only rejected documents can be re-uploaded."
                );

            }

            // Delete old file if it exists
            try {

                Path oldFile = Paths.get(document.getFilePath());

                if (Files.exists(oldFile)) {

                    Files.delete(oldFile);

                }

            } catch (IOException ex) {

                log.warn("Unable to delete old document : {}",
                        document.getFilePath());

            }

            // Save new file
            String savedPath = saveFile(file);

            // Update document details
            document.setFileName(file.getOriginalFilename());

            document.setFilePath(savedPath);

            document.setStatus(DocumentStatus.PENDING);

            document.setRejectionReason(null);

            document.setUpdatedAt(LocalDateTime.now());

            candidateDocumentRepository.save(document);

            log.info(
                    "{} re-uploaded successfully by {}",
                    documentType,
                    candidate.getEmail()
            );

        } catch (IOException e) {

            log.error("Document re-upload failed", e);

            throw new RuntimeException(
                    "Unable to re-upload document."
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

        Candidate candidate = candidateRepository
                .findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Candidate not found."
                        ));

        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();

    }

    @Override
    public CandidateDocumentDto getDocument(
            Long documentId
    ) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found."
                                ));

        return mapToDto(document);

    }
//    deleteDocument()
    @Override
    public void deleteDocument(
            Long documentId
    ) {

        Candidate candidate = getLoggedInCandidate();

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found."
                                ));

        if (!document.getCandidate().getId()
                .equals(candidate.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this document."
            );

        }
     // Improvement: Prevent deleting verified documents
        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new RuntimeException(
                    "Verified documents cannot be deleted."
            );
        }

        try {

            Path file = Paths.get(document.getFilePath());

            if (Files.exists(file)) {

                Files.delete(file);

            }

        } catch (IOException e) {

            log.warn("Unable to delete file : {}", document.getFilePath());

        }

        candidateDocumentRepository.delete(document);

        log.info("Document deleted successfully.");

    }
//    verifyDocument()
    @Override
    public void verifyDocument(Long documentId) {

        CandidateDocument document = candidateDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found."));

        // Prevent verifying again
        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new RuntimeException("Document is already verified.");
        }

        document.setStatus(DocumentStatus.VERIFIED);
        document.setRejectionReason(null);
        document.setUpdatedAt(LocalDateTime.now());

        candidateDocumentRepository.save(document);

        log.info("{} verified successfully.",
                document.getDocumentType());
    }
//    rejectDocument()
    @Override
    public void rejectDocument(
            Long documentId,
            String rejectionReason
    ) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found."
                                ));

        // Prevent rejecting a verified document
        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new RuntimeException(
                    "Verified document cannot be rejected."
            );
        }

       

        document.setStatus(DocumentStatus.REJECTED);

        document.setRejectionReason(rejectionReason);

        document.setUpdatedAt(LocalDateTime.now());

        candidateDocumentRepository.save(document);

        log.info("{} rejected.",
                document.getDocumentType());
    }
 // Fetch all pending documents for HR review.
        @Override
        public List<CandidateDocumentDto> getPendingDocuments() {

            return candidateDocumentRepository
                    .findByStatus(DocumentStatus.PENDING)
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }
        
        @Override
        public Resource downloadDocument(Long documentId) {

            CandidateDocument document = candidateDocumentRepository
                    .findById(documentId)
                    .orElseThrow(() ->
                            new RuntimeException("Document not found."));

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            UserPrincipal principal =
                    (UserPrincipal) authentication.getPrincipal();

            // Candidate can access only their own documents
            if ("CANDIDATE".equals(principal.getRole().name())) {

                if (!document.getCandidate().getId().equals(principal.getUserId())) {

                    throw new RuntimeException(
                            "You are not authorized to access this document.");

                }
            }

            try {

                Path path = Paths.get(document.getFilePath());

                Resource resource = new UrlResource(path.toUri());

                if (!resource.exists() || !resource.isReadable()) {
                    throw new RuntimeException("File not found.");
                }

                return resource;

            } catch (Exception e) {

                log.error("Unable to download document.", e);

                throw new RuntimeException("Unable to download document.");
            }
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

            if (candidates.isEmpty()) {
                return new ArrayList<>();
            }

            List<CandidateDashboardDto> dashboardList = new ArrayList<>();

            for (Candidate candidate : candidates) {

                List<CandidateDocument> documents =
                        candidateDocumentRepository.findByCandidateId(candidate.getId());

                long verified = documents.stream()
                        .filter(document -> document.getStatus() == DocumentStatus.VERIFIED)
                        .count();

                long pending = documents.stream()
                        .filter(document -> document.getStatus() == DocumentStatus.PENDING)
                        .count();

                long rejected = documents.stream()
                        .filter(document -> document.getStatus() == DocumentStatus.REJECTED)
                        .count();

                dashboardList.add(
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

            return dashboardList;
        }
}
