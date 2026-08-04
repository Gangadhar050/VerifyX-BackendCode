package com.verify_x.serviceImpl;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Education;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EducationServiceImplTest {

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private CandidateRepository candidateRepository;

    private EducationServiceImpl educationService;

    @BeforeEach
    void setUp() {

        educationService = new EducationServiceImpl(
                educationRepository,
                candidateRepository
        );

        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSaveEducationSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        Education education = new Education();
        education.setCandidate(candidate);

        when(educationRepository.save(any(Education.class)))
                .thenReturn(education);

        EducationRequest request = new EducationRequest();
        request.setTenthSchoolName("ABC School");

        EducationResponse response =
                educationService.saveEducation(request);

        assertNotNull(response);

        verify(educationRepository)
                .save(any(Education.class));
    }

    @Test
    void testSaveEducationAlreadyExists() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(true);

        EducationRequest request = new EducationRequest();

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> educationService.saveEducation(request));

        assertEquals(
                "Education details already exist.",
                exception.getMessage());
    }

    @Test
    void testUpdateEducationSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        Education education = new Education();
        education.setCandidate(candidate);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(education));

        when(educationRepository.save(any(Education.class)))
                .thenReturn(education);

        EducationRequest request = new EducationRequest();
        request.setDegreeName("B.E");

        EducationResponse response =
                educationService.updateEducation(request);

        assertNotNull(response);

        verify(educationRepository)
                .save(any(Education.class));
    }

    @Test
    void testUpdateEducationNotFound() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        EducationRequest request = new EducationRequest();

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> educationService.updateEducation(request));

        assertEquals(
                "Education details not found.",
                exception.getMessage());
    }

    @Test
    void testSaveEducationWithPdfFile() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        when(educationRepository.save(any(Education.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile pdf =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "10th.pdf",
                        "application/pdf",
                        "dummy".getBytes());

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(pdf);

        EducationResponse response =
                educationService.saveEducation(request);

        assertNotNull(response);

        verify(educationRepository)
                .save(any(Education.class));
    }
    @Test
    void testGetMyEducationSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .technicalSkills(new ArrayList<>())
                .build();

        Education education = new Education();
        education.setId(1L);
        education.setCandidate(candidate);
        education.setTenthSchoolName("ABC School");
        education.setDegreeName("B.E");

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(education));

        EducationResponse response =
                educationService.getMyEducation();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABC School", response.getTenthSchoolName());
        assertEquals("B.E", response.getDegreeName());

        verify(educationRepository)
                .findByCandidate(candidate);
    }
    @Test
    void testGetMyEducationNotFound() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> educationService.getMyEducation());

        assertEquals(
                "Education details not found.",
                exception.getMessage());

        verify(educationRepository)
                .findByCandidate(candidate);
    }
    @Test
    void testGetEducationByCandidateIdSuccess() {

        Candidate candidate = Candidate.builder()
                .id(2L)
                .technicalSkills(new ArrayList<>())
                .build();

        Education education = new Education();
        education.setId(5L);
        education.setCandidate(candidate);
        education.setDegreeName("M.Tech");

        when(educationRepository.findByCandidateId(2L))
                .thenReturn(Optional.of(education));

        EducationResponse response =
                educationService.getEducationByCandidateId(2L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("M.Tech", response.getDegreeName());

        verify(educationRepository)
                .findByCandidateId(2L);
    }
    @Test
    void testGetEducationByCandidateIdNotFound() {

        when(educationRepository.findByCandidateId(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> educationService.getEducationByCandidateId(100L));

        assertEquals(
                "Education details not found.",
                exception.getMessage());

        verify(educationRepository)
                .findByCandidateId(100L);
    }
    @Test
    void testGetMyEducationWithTechnicalSkills() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .technicalSkills(new ArrayList<>())
                .build();

        Education education = new Education();
        education.setCandidate(candidate);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(education));

        EducationResponse response =
                educationService.getMyEducation();

        assertEquals(
                "Java, Spring Boot",
                response.getTechnicalSkills());
    }
    @Test
    void testViewTenthMarksCardSuccess() {

        Education education = new Education();
        education.setId(1L);
        education.setTenthMarksCard("dummy".getBytes());

        when(educationRepository.findById(1L))
                .thenReturn(Optional.of(education));

        Resource resource = educationService.viewDocument(
                1L,
                EducationDocumentType.TENTH_MARKS_CARD);

        assertNotNull(resource);
        verify(educationRepository).findById(1L);
    }
    @Test
    void testViewMastersDegreeCertificateSuccess() {

        Education education = new Education();
        education.setMastersDegreeCertificate("masters".getBytes());

        when(educationRepository.findById(1L))
                .thenReturn(Optional.of(education));

        Resource resource = educationService.viewDocument(
                1L,
                EducationDocumentType.MASTERS_DEGREE_CERTIFICATE);

        assertNotNull(resource);
    }
    @Test
    void testViewDocumentEducationNotFound() {

        when(educationRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.viewDocument(
                        100L,
                        EducationDocumentType.TENTH_MARKS_CARD)
        );
    }
    @Test
    void testViewDocumentFileNotFound() {

        Education education = new Education();
        education.setTenthMarksCard(null);

        when(educationRepository.findById(1L))
                .thenReturn(Optional.of(education));

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.viewDocument(
                        1L,
                        EducationDocumentType.TENTH_MARKS_CARD)
        );
    }
    @Test
    void testDeleteEducationNotFound() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> educationService.deleteEducation()
        );

        verify(educationRepository, never())
                .delete(any());
    }
    @Test
    void testViewTwelfthMarksCardSuccess() {

        Education education = new Education();
        education.setTwelfthMarksCard("twelfth".getBytes());

        when(educationRepository.findById(1L))
                .thenReturn(Optional.of(education));

        Resource resource = educationService.viewDocument(
                1L,
                EducationDocumentType.TWELFTH_MARKS_CARD);

        assertNotNull(resource);
    }
    @Test
    void testViewMastersMarksCardSuccess() {

        Education education = new Education();
        education.setMastersMarksCard("masters".getBytes());

        when(educationRepository.findById(1L))
                .thenReturn(Optional.of(education));

        Resource resource = educationService.viewDocument(
                1L,
                EducationDocumentType.MASTERS_MARKS_CARD);

        assertNotNull(resource);
    }
    @Test
    void testSaveEducationInvalidContentType() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        MockMultipartFile file =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "marks.txt",
                        "text/plain",
                        "dummy".getBytes());

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(file);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> educationService.saveEducation(request));

        assertEquals(
                "Only PDF, JPG, JPEG and PNG files are allowed.",
                exception.getMessage());
    }
    @Test
    void testSaveEducationFileTooLarge() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        byte[] data = new byte[6 * 1024 * 1024];

        MockMultipartFile file =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "large.pdf",
                        "application/pdf",
                        data);

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(file);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> educationService.saveEducation(request));

        assertEquals(
                "Maximum allowed file size is 5 MB.",
                exception.getMessage());
    }
    @Test
    void testSaveEducationJpgSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        when(educationRepository.save(any(Education.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "photo.jpg",
                        "image/jpg",
                        "abc".getBytes());

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(file);

        EducationResponse response =
                educationService.saveEducation(request);

        assertNotNull(response);

        verify(educationRepository)
                .save(any(Education.class));
    }
    @Test
    void testSaveEducationPngSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        when(educationRepository.save(any(Education.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "photo.png",
                        "image/png",
                        "abc".getBytes());

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(file);

        EducationResponse response =
                educationService.saveEducation(request);

        assertNotNull(response);
    }
    @Test
    void testSaveEducationEmptyFile() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.existsByCandidate(candidate))
                .thenReturn(false);

        when(educationRepository.save(any(Education.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile(
                        "tenthMarksCard",
                        "",
                        "application/pdf",
                        new byte[0]);

        EducationRequest request = new EducationRequest();
        request.setTenthMarksCard(file);

        EducationResponse response =
                educationService.saveEducation(request);

        assertNotNull(response);
    }
    @Test
    void testUpdateEducationWithPdf() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        Education education = new Education();
        education.setCandidate(candidate);

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(education));

        when(educationRepository.save(any(Education.class)))
                .thenReturn(education);

        MockMultipartFile file =
                new MockMultipartFile(
                        "degreeCertificate",
                        "degree.pdf",
                        "application/pdf",
                        "pdf".getBytes());

        EducationRequest request = new EducationRequest();
        request.setDegreeCertificate(file);

        EducationResponse response =
                educationService.updateEducation(request);

        assertNotNull(response);

        verify(educationRepository)
                .save(any(Education.class));
    }

}