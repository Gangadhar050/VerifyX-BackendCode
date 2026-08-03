package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateDocumentRequest;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateDocumentServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @InjectMocks
    private CandidateDocumentServiceImpl candidateDocumentService;

    private Candidate candidate;

    @BeforeEach
    void setup() {

        candidate = Candidate.builder()
                .id(1L)
                .username("Gangadhar")
                .email("gangadhar@gmail.com")
                .candidateType(CandidateType.EXPERIENCED)
                .build();
    }

    /**
     * Creates logged-in user
     */
    private MockedStatic<SecurityContextHolder> mockLoggedInUser() {

        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L)
                .email(candidate.getEmail())
                .username(candidate.getUsername())
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(authentication);

        MockedStatic<SecurityContextHolder> mocked =
                mockStatic(SecurityContextHolder.class);

        mocked.when(SecurityContextHolder::getContext)
                .thenReturn(context);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        return mocked;
    }

    @Test
    void uploadDocuments_ShouldUploadResumeSuccessfully() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.pdf",
                            "application/pdf",
                            "dummy".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            when(candidateDocumentRepository.existsByCandidateAndDocumentType(
                    candidate,
                    DocumentType.RESUME))
                    .thenReturn(false);

            candidateDocumentService.uploadDocuments(request);

            verify(candidateDocumentRepository)
                    .save(any(CandidateDocument.class));

            verify(candidateRepository)
                    .save(candidate);
        }
    }

    @Test
    void uploadDocuments_ShouldThrow_WhenResumeAlreadyExists() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.pdf",
                            "application/pdf",
                            "dummy".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            when(candidateDocumentRepository.existsByCandidateAndDocumentType(
                    candidate,
                    DocumentType.RESUME))
                    .thenReturn(true);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> candidateDocumentService.uploadDocuments(request));

            assertEquals(
                    "RESUME already uploaded.",
                    ex.getMessage());

            verify(candidateDocumentRepository, never())
                    .save(any());
        }
    }

    @Test
    void uploadDocuments_ShouldThrow_WhenInvalidFileType() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.exe",
                            "application/octet-stream",
                            "abc".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> candidateDocumentService.uploadDocuments(request));

            assertTrue(
                    ex.getMessage().contains("Only PDF"));
        }
    }

    @Test
    void getMyDocuments_ShouldReturnDocuments() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            CandidateDocument document =
                    CandidateDocument.builder()
                            .id(10L)
                            .candidate(candidate)
                            .documentType(DocumentType.RESUME)
                            .fileName("resume.pdf")
                            .contentType("application/pdf")
                            .status(DocumentStatus.PENDING)
                            .build();

            when(candidateDocumentRepository.findByCandidate(candidate))
                    .thenReturn(List.of(document));

            List<CandidateDocumentDto> response =
                    candidateDocumentService.getMyDocuments();

            assertEquals(1, response.size());

            assertEquals(
                    DocumentType.RESUME,
                    response.get(0).getDocumentType());

            assertEquals(
                    "resume.pdf",
                    response.get(0).getFileName());
        }
    }@Test
    void reUploadDocuments_ShouldReUploadRejectedResumeSuccessfully() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume_new.pdf",
                            "application/pdf",
                            "updated".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            CandidateDocument document =
                    CandidateDocument.builder()
                            .id(1L)
                            .candidate(candidate)
                            .documentType(DocumentType.RESUME)
                            .status(DocumentStatus.REJECTED)
                            .rejectionReason("Blur image")
                            .build();

            when(candidateDocumentRepository.findByCandidateAndDocumentType(
                    candidate,
                    DocumentType.RESUME))
                    .thenReturn(Optional.of(document));

            when(candidateDocumentRepository.findByCandidate(candidate))
                    .thenReturn(List.of(document));

            candidateDocumentService.reUploadDocuments(request);

            verify(candidateDocumentRepository)
                    .save(document);

            assertEquals(DocumentStatus.PENDING, document.getStatus());

            assertNull(document.getRejectionReason());
        }
    }

    @Test
    void reUploadDocuments_ShouldThrow_WhenDocumentNotRejected() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.pdf",
                            "application/pdf",
                            "abc".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            CandidateDocument document =
                    CandidateDocument.builder()
                            .candidate(candidate)
                            .documentType(DocumentType.RESUME)
                            .status(DocumentStatus.PENDING)
                            .build();

            when(candidateDocumentRepository.findByCandidateAndDocumentType(
                    candidate,
                    DocumentType.RESUME))
                    .thenReturn(Optional.of(document));

            RuntimeException ex =
                    assertThrows(RuntimeException.class,
                            () -> candidateDocumentService.reUploadDocuments(request));

            assertTrue(ex.getMessage().contains("Only rejected documents"));
        }
    }

    @Test
    void reUploadDocuments_ShouldThrow_WhenDocumentNotFound() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.pdf",
                            "application/pdf",
                            "abc".getBytes());

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            when(candidateDocumentRepository.findByCandidateAndDocumentType(
                    candidate,
                    DocumentType.RESUME))
                    .thenReturn(Optional.empty());

            RuntimeException ex =
                    assertThrows(RuntimeException.class,
                            () -> candidateDocumentService.reUploadDocuments(request));

            assertEquals("RESUME not found.", ex.getMessage());
        }
    }

    @Test
    void uploadDocuments_ShouldThrow_WhenFileTooLarge() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            byte[] data = new byte[6 * 1024 * 1024];

            MockMultipartFile resume =
                    new MockMultipartFile(
                            "resume",
                            "resume.pdf",
                            "application/pdf",
                            data);

            CandidateDocumentRequest request =
                    CandidateDocumentRequest.builder()
                            .resume(resume)
                            .build();

            RuntimeException ex =
                    assertThrows(RuntimeException.class,
                            () -> candidateDocumentService.uploadDocuments(request));

            assertEquals("Maximum allowed size is 5 MB.", ex.getMessage());
        }
    }
//
//    @Test
//    void uploadDocuments_ShouldThrow_WhenFileIsEmpty() {
//
//        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {
//
//            MockMultipartFile resume =
//                    new MockMultipartFile(
//                            "resume",
//                            "",
//                            "application/pdf",
//                            new byte[0]);
//
//            CandidateDocumentRequest request =
//                    CandidateDocumentRequest.builder()
//                            .resume(resume)
//                            .build();
//
//            RuntimeException ex =
//                    assertThrows(RuntimeException.class,
//                            () -> candidateDocumentService.uploadDocuments(request));
//
//            assertEquals("Please upload a document.", ex.getMessage());
//        }
//    }

    @Test
    void getMyDocuments_ShouldReturnEmptyList() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            when(candidateDocumentRepository.findByCandidate(candidate))
                    .thenReturn(List.of());

            List<CandidateDocumentDto> response =
                    candidateDocumentService.getMyDocuments();

            assertTrue(response.isEmpty());
        }
    }
    @Test
    void getDocumentsByCandidateId_ShouldReturnDocuments() {

        CandidateDocument document = CandidateDocument.builder()
                .id(10L)
                .candidate(candidate)
                .documentType(DocumentType.RESUME)
                .fileName("resume.pdf")
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(document));

        List<CandidateDocumentDto> response =
                candidateDocumentService.getDocumentsByCandidateId(1L);

        assertEquals(1, response.size());
        assertEquals(DocumentType.RESUME,
                response.get(0).getDocumentType());
    }
    @Test
    void getDocumentsByCandidateId_ShouldThrow_WhenCandidateNotFound() {

        when(candidateRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException ex =
                assertThrows(RuntimeException.class,
                        () -> candidateDocumentService.getDocumentsByCandidateId(100L));

        assertEquals("Candidate not found.", ex.getMessage());
    }

    @Test
    void getDocument_ShouldReturnDocument() {

        CandidateDocument document = CandidateDocument.builder()
                .id(5L)
                .documentType(DocumentType.RESUME)
                .build();

        when(candidateDocumentRepository.findById(5L))
                .thenReturn(Optional.of(document));

        CandidateDocument result =
                candidateDocumentService.getDocument(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void deleteDocument_ShouldDeleteSuccessfully() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            CandidateDocument document =
                    CandidateDocument.builder()
                            .id(1L)
                            .candidate(candidate)
                            .documentType(DocumentType.RESUME)
                            .status(DocumentStatus.PENDING)
                            .build();

            when(candidateDocumentRepository.findById(1L))
                    .thenReturn(Optional.of(document));

            candidateDocumentService.deleteDocument(1L);

            verify(candidateDocumentRepository)
                    .delete(document);
        }
    }

    @Test
    void deleteDocument_ShouldThrow_WhenVerified() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            CandidateDocument document =
                    CandidateDocument.builder()
                            .candidate(candidate)
                            .status(DocumentStatus.VERIFIED)
                            .build();

            when(candidateDocumentRepository.findById(1L))
                    .thenReturn(Optional.of(document));

            RuntimeException ex =
                    assertThrows(RuntimeException.class,
                            () -> candidateDocumentService.deleteDocument(1L));

            assertEquals(
                    "Verified documents cannot be deleted.",
                    ex.getMessage());
        }
    }
    @Test
    void deleteDocument_ShouldThrow_WhenAnotherCandidateOwnsDocument() {

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {

            Candidate another =
                    Candidate.builder()
                            .id(99L)
                            .build();

            CandidateDocument document =
                    CandidateDocument.builder()
                            .candidate(another)
                            .status(DocumentStatus.PENDING)
                            .build();

            when(candidateDocumentRepository.findById(1L))
                    .thenReturn(Optional.of(document));

            RuntimeException ex =
                    assertThrows(RuntimeException.class,
                            () -> candidateDocumentService.deleteDocument(1L));

            assertEquals(
                    "You are not allowed to delete this document.",
                    ex.getMessage());
        }
    }
//    @Test
//    void downloadDocument_ShouldReturnResource() {
//
//        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedInUser()) {
//
//            CandidateDocument document =
//                    CandidateDocument.builder()
//                            .candidate(candidate)
//                            .documentData("Hello".getBytes())
//                            .build();
//
//            when(candidateDocumentRepository.findById(1L))
//                    .thenReturn(Optional.of(document));
//
//            Resource resource =
//                    candidateDocumentService.downloadDocument(1L);
//
//            assertNotNull(resource);
//        }
//    }
}