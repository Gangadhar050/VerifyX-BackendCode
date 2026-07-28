package com.verify_x.serviceImpl;
import com.verify_x.entity.Employment;
import com.verify_x.enums.VerificationStatus;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.HrVerificationRequestDto;
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
    @Override
    public void verifyUan(Long candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found."));

        Employment employment = candidate.getEmployment();

        if (employment == null) {
            throw new RuntimeException("Employment details not found.");
        }

        employment.setUanVerificationStatus(VerificationStatus.VERIFIED);
    }
    @Override
    public void verifyDocument(
            Long documentId
    ) {

        CandidateDocument document =
                candidateDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found."
                                ));

        document.setStatus(DocumentStatus.VERIFIED);

        document.setRejectionReason(null);

        document.setUpdatedAt(LocalDateTime.now());

        candidateDocumentRepository.save(document);

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
                                new RuntimeException(
                                        "Document not found."
                                ));

        document.setStatus(DocumentStatus.REJECTED);

        document.setRejectionReason(rejectionReason);

        document.setUpdatedAt(LocalDateTime.now());

        candidateDocumentRepository.save(document);

        log.info("{} rejected.",
                document.getDocumentType());

    }
    @Override
    public List<HrVerificationRequestDto> getAllVerificationRequests() {

        return candidateRepository.findAll()
                .stream()
                .filter(candidate ->
                        candidateDocumentRepository.countByCandidate(candidate) > 0)
                .map(candidate -> {

                    Long documentCount =
                            candidateDocumentRepository.countByCandidate(candidate);

                    List<CandidateDocument> documents =
                            candidateDocumentRepository.findByCandidate(candidate);

                    DocumentStatus overallStatus = DocumentStatus.PENDING;

                    if (!documents.isEmpty()) {

                        boolean rejected = documents.stream()
                                .anyMatch(doc -> doc.getStatus() == DocumentStatus.REJECTED);

                        boolean pending = documents.stream()
                                .anyMatch(doc -> doc.getStatus() == DocumentStatus.PENDING);

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
                            .documentCount(documentCount.intValue())
                            .uanVerificationStatus(
                                    candidate.getEmployment() != null
                                            ? candidate.getEmployment().getUanVerificationStatus()
                                            : VerificationStatus.PENDING
                            )
                            .status(overallStatus)
                            .build();
                })
                .toList();
    }
    }
