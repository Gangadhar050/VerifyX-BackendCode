package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateDocumentRequest;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentType;
import com.verify_x.enums.Role;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void setUp() {

        candidate = Candidate.builder()
                .id(1L)
                .email("john@test.com")
                .candidateType(CandidateType.FRESHER)
                .build();

        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L)
                .role(Role.CANDIDATE)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUploadResumeSuccess() {

        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        CandidateDocumentRequest request = CandidateDocumentRequest.builder()
                .resume(resume)
                .build();

        when(candidateDocumentRepository.existsByCandidateAndDocumentType(
                candidate,
                DocumentType.RESUME))
                .thenReturn(false);

        candidateDocumentService.uploadDocuments(request);

        verify(candidateDocumentRepository, times(1))
                .save(any(CandidateDocument.class));

        verify(candidateRepository).save(candidate);
    }
    @Test
    void testUploadInvalidFileType() {

        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.txt",
                "text/plain",
                "dummy".getBytes()
        );

        CandidateDocumentRequest request = CandidateDocumentRequest.builder()
                .resume(resume)
                .build();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidateDocumentService.uploadDocuments(request)
        );

        assertEquals(
                "Only PDF, PNG, JPG and JPEG files are allowed.",
                exception.getMessage()
        );
    }
}