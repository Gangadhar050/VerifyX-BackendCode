package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Employment;
import com.verify_x.entity.Education;
import com.verify_x.enums.*;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import com.verify_x.repository.EmploymentRepository;
import com.verify_x.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateManagementServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private EmailService emailService;

    private CandidateManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CandidateManagementServiceImpl(
                candidateRepository,
                employmentRepository,
                candidateDocumentRepository,
                educationRepository,
                emailService
        );
    }

    @Test
    void testGetAllCandidatesSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .email("john@test.com")
                .phoneNumber("9876543210")
                .candidateType(CandidateType.FRESHER)
                .technicalSkills(List.of())
                .applicationStatus(ApplicationStatus.PENDING_VERIFICATION)
                .build();

        when(candidateRepository.findAll())
                .thenReturn(List.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        List<CandidateSummaryDto> result = service.getAllCandidates();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFullName());

        verify(candidateRepository).findAll();
    }

    @Test
    void testGetAllCandidatesEmptyList() {

        when(candidateRepository.findAll())
                .thenReturn(List.of());

        List<CandidateSummaryDto> result = service.getAllCandidates();

        assertTrue(result.isEmpty());

        verify(candidateRepository).findAll();
    }

    @Test
    void testSearchCandidatesSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("Alice")
                .email("alice@test.com")
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "alice",
                        "alice"))
                .thenReturn(List.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        List<CandidateSummaryDto> result =
                service.searchCandidates("alice");

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getFullName());
    }

    @Test
    void testSearchCandidatesNoResult() {

        when(candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        anyString(),
                        anyString()))
                .thenReturn(List.of());

        List<CandidateSummaryDto> result =
                service.searchCandidates("xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCandidateDetailsSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .email("john@test.com")
                .phoneNumber("9999999999")
                .candidateType(CandidateType.FRESHER)
                .build();

        Employment employment = Employment.builder()
                .previousCompanyName("ABC")
                .uanVerified(true)
                .build();

        CandidateDocument document = CandidateDocument.builder()
                .id(10L)
                .documentType(DocumentType.RESUME)
                .status(DocumentStatus.VERIFIED)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(document));

        when(educationRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        CandidateDetailsDto dto =
                service.getCandidateDetails(1L);

        assertNotNull(dto);
        assertEquals("John",
                dto.getProfile().getUsername());
        assertEquals(1,
                dto.getDocuments().size());

        verify(candidateRepository).findById(1L);
    }

    @Test
    void testGetCandidateDetailsCandidateNotFound() {

        when(candidateRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getCandidateDetails(100L)
        );
    }
    @Test
    void testCreateCandidateSuccess() {

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .username("John")
                .email("john@test.com")
                .phoneNumber("9876543210")
                .password("password")
                .appliedRole("Java Developer")
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);

        when(candidateRepository.existsByPhoneNumber(dto.getPhoneNumber()))
                .thenReturn(false);

        CandidateSummaryDto result = service.createCandidate(dto);

        assertNotNull(result);
        assertEquals("John", result.getFullName());
        assertEquals("john@test.com", result.getEmail());

        verify(candidateRepository).save(any(Candidate.class));
    }
    @Test
    void testCreateCandidateEmailAlreadyExists() {

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .email("john@test.com")
                .phoneNumber("9999999999")
                .build();

        when(candidateRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> service.createCandidate(dto)
        );

        verify(candidateRepository, never())
                .save(any());
    }
    @Test
    void testCreateCandidatePhoneAlreadyExists() {

        UserRegistrationDto dto = UserRegistrationDto.builder()
                .email("john@test.com")
                .phoneNumber("9999999999")
                .build();

        when(candidateRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);

        when(candidateRepository.existsByPhoneNumber(dto.getPhoneNumber()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> service.createCandidate(dto)
        );

        verify(candidateRepository, never())
                .save(any());
    }
    @Test
    void testDeleteCandidateSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        CandidateDocument document = CandidateDocument.builder()
                .id(100L)
                .build();

        Employment employment = Employment.builder()
                .id(10L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(document));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        service.deleteCandidate(1L);

        verify(candidateDocumentRepository)
                .delete(document);

        verify(employmentRepository)
                .delete(employment);

        verify(candidateRepository)
                .delete(candidate);
    }
    @Test
    void testDeleteCandidateWithoutEmployment() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of());

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        service.deleteCandidate(1L);

        verify(candidateRepository)
                .delete(candidate);

        verify(employmentRepository, never())
                .delete(any());
    }
    @Test
    void testDeleteCandidateNotFound() {

        when(candidateRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteCandidate(100L)
        );

        verify(candidateRepository, never())
                .delete(any());
    }
    @Test
    void testVerifyUanRejectedSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        Employment employment = Employment.builder()
                .id(1L)
                .uanNumber("123456789012")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        service.verifyUan(
                1L,
                VerificationStatus.REJECTED,
                "HR"
        );

        assertFalse(employment.getUanVerified());

        assertEquals(
                VerificationStatus.REJECTED,
                employment.getUanVerificationStatus());

        assertEquals(
                "HR",
                employment.getUanVerifiedBy());

        assertNotNull(employment.getUanVerifiedAt());

        verify(employmentRepository).save(employment);
    }
    @Test
    void testVerifyUanPendingSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        Employment employment = Employment.builder()
                .id(1L)
                .uanNumber("123456789012")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        service.verifyUan(
                1L,
                VerificationStatus.PENDING,
                "HR"
        );

        assertFalse(employment.getUanVerified());

        assertEquals(
                VerificationStatus.PENDING,
                employment.getUanVerificationStatus());

        assertNull(employment.getUanVerifiedBy());

        assertNull(employment.getUanVerifiedAt());

        verify(employmentRepository).save(employment);
    }
    @Test
    void testVerifyUanCandidateNotFound() {

        when(candidateRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.verifyUan(
                        100L,
                        VerificationStatus.VERIFIED,
                        "HR")
        );

        verify(employmentRepository, never())
                .save(any());
    }
    @Test
    void testVerifyUanEmploymentNotFound() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.verifyUan(
                        1L,
                        VerificationStatus.VERIFIED,
                        "HR")
        );

        verify(employmentRepository, never())
                .save(any());
    }
    @Test
    void testUpdateApplicationStatusApprovedForFresherSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .email("john@test.com")
                .username("John")
                .candidateType(CandidateType.FRESHER)
                .build();

        CandidateDocument resume = CandidateDocument.builder()
                .documentType(DocumentType.RESUME)
                .status(DocumentStatus.VERIFIED)
                .build();

        CandidateDocument pan = CandidateDocument.builder()
                .documentType(DocumentType.PAN_CARD)
                .status(DocumentStatus.VERIFIED)
                .build();

        ApplicationStatusUpdateDto dto =
                ApplicationStatusUpdateDto.builder()
                        .status(ApplicationStatus.APPROVED)
                        .remarks("Approved")
                        .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(resume, pan));

        service.updateApplicationStatus(1L, dto, "HR");

        assertEquals(ApplicationStatus.APPROVED,
                candidate.getApplicationStatus());

        verify(candidateRepository).save(candidate);

        verify(emailService)
                .sendApplicationApprovedEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        dto.getRemarks());
    }

    @Test
    void testApproveWithRejectedDocuments() {

        Candidate candidate = Candidate.builder()
                .candidateType(CandidateType.FRESHER)
                .build();

        CandidateDocument document =
                CandidateDocument.builder()
                        .documentType(DocumentType.RESUME)
                        .status(DocumentStatus.REJECTED)
                        .build();

        when(candidateRepository.findById(anyLong()))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(document));

        ApplicationStatusUpdateDto dto =
                ApplicationStatusUpdateDto.builder()
                        .status(ApplicationStatus.APPROVED)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> service.updateApplicationStatus(
                        1L,
                        dto,
                        "HR")
        );
    }
    @Test
    void testApproveWithPendingDocuments() {

        Candidate candidate = Candidate.builder()
                .candidateType(CandidateType.FRESHER)
                .build();

        CandidateDocument document =
                CandidateDocument.builder()
                        .documentType(DocumentType.RESUME)
                        .status(DocumentStatus.PENDING)
                        .build();

        when(candidateRepository.findById(anyLong()))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        when(candidateDocumentRepository.findByCandidate(candidate))
                .thenReturn(List.of(document));

        ApplicationStatusUpdateDto dto =
                ApplicationStatusUpdateDto.builder()
                        .status(ApplicationStatus.APPROVED)
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> service.updateApplicationStatus(
                        1L,
                        dto,
                        "HR")
        );
    }
    @Test
    void testRejectCandidateSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .email("john@test.com")
                .username("John")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        ApplicationStatusUpdateDto dto =
                ApplicationStatusUpdateDto.builder()
                        .status(ApplicationStatus.REJECTED)
                        .remarks("Not Eligible")
                        .build();

        service.updateApplicationStatus(
                1L,
                dto,
                "HR");

        assertEquals(
                ApplicationStatus.REJECTED,
                candidate.getApplicationStatus());

        verify(emailService)
                .sendApplicationRejectedEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        "Not Eligible");
    }
    @Test
    void testReUploadRequiredSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .email("john@test.com")
                .username("John")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        ApplicationStatusUpdateDto dto =
                ApplicationStatusUpdateDto.builder()
                        .status(ApplicationStatus.RE_UPLOAD_REQUIRED)
                        .remarks("Upload PAN")
                        .build();

        service.updateApplicationStatus(
                1L,
                dto,
                "HR");

        assertEquals(
                ApplicationStatus.RE_UPLOAD_REQUIRED,
                candidate.getApplicationStatus());

        verify(emailService)
                .sendReUploadRequestEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        "Upload PAN");
    }

}