package com.verify_x.serviceImpl;

import com.verify_x.dto.EmploymentDetailsDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Employment;
import com.verify_x.enums.*;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EmploymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmploymentServiceImplTest {

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private CandidateRepository candidateRepository;

    private EmploymentServiceImpl employmentService;

    @BeforeEach
    void setUp() {

        employmentService =
                new EmploymentServiceImpl(
                        employmentRepository,
                        candidateRepository);

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
    void testSaveEmploymentDetailsExperiencedSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(3.0)
                .lastCTC(500000.0)
                .lastWorkingDay(LocalDate.now())
                .uanNumber("123456789012")
                .employmentStatus(EmploymentStatus.NOT_CURRENTLY_EMPLOYED)
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        employmentService.saveEmploymentDetails(dto);

        verify(employmentRepository)
                .save(any(Employment.class));
    }

    @Test
    void testSaveEmploymentDetailsFresherSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .offerLetterStatus(
                        OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.empty());

        employmentService.saveEmploymentDetails(dto);

        verify(employmentRepository)
                .save(any(Employment.class));
    }

    @Test
    void testUpdateEmploymentDetailsSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .offerLetterStatus(
                        OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(new Employment()));

        employmentService.updateEmploymentDetails(dto);

        verify(employmentRepository)
                .save(any(Employment.class));
    }

    @Test
    void testSaveEmploymentCandidateNotFound() {

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(
                        EmploymentDetailsDto.builder().build())
        );
    }

    @Test
    void testUpdateEmploymentCandidateNotFound() {

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> employmentService.updateEmploymentDetails(
                        EmploymentDetailsDto.builder().build())
        );
    }
    @Test
    void testGetEmploymentDetailsByCandidateIdSuccess() {

        Employment employment = Employment.builder()
                .previousCompanyName("Infosys")
                .previousDesignation("Developer")
                .totalExperience(2.5)
                .lastCTC(500000.0)
                .uanNumber("123456789012")
                .build();

        when(employmentRepository.findByCandidateId(1L))
                .thenReturn(Optional.of(employment));

        EmploymentDetailsDto dto =
                employmentService.getEmploymentDetailsByCandidateId(1L);

        assertNotNull(dto);
        assertEquals("Infosys", dto.getPreviousCompanyName());
        assertEquals("Developer", dto.getPreviousDesignation());

        verify(employmentRepository).findByCandidateId(1L);
    }
    @Test
    void testGetEmploymentDetailsByCandidateIdNotFound() {

        when(employmentRepository.findByCandidateId(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.getEmploymentDetailsByCandidateId(1L));

        assertEquals(
                "Employment details not found",
                exception.getMessage());
    }
    @Test
    void testGetEmploymentDetailsByEmailSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .email("john@test.com")
                .build();

        Employment employment = Employment.builder()
                .previousCompanyName("TCS")
                .previousDesignation("Software Engineer")
                .build();

        when(candidateRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        EmploymentDetailsDto dto =
                employmentService.getEmploymentDetailsByEmail("john@test.com");

        assertNotNull(dto);
        assertEquals("TCS", dto.getPreviousCompanyName());

        verify(candidateRepository).findByEmail("john@test.com");
    }
    @Test
    void testGetEmploymentDetailsByEmailCandidateNotFound() {

        when(candidateRepository.findByEmail("abc@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.getEmploymentDetailsByEmail("abc@test.com"));

        assertEquals(
                "Candidate not found",
                exception.getMessage());
    }
    @Test
    void testSearchEmploymentDetailsSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .email("john@test.com")
                .build();

        Employment employment = Employment.builder()
                .previousCompanyName("IBM")
                .build();

        when(candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "john",
                        "john"))
                .thenReturn(List.of(candidate));

        when(employmentRepository.findByCandidate(candidate))
                .thenReturn(Optional.of(employment));

        List<EmploymentDetailsDto> result =
                employmentService.searchEmploymentDetails("john");

        assertEquals(1, result.size());

        verify(candidateRepository)
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "john",
                        "john");
    }
    @Test
    void testSearchEmploymentDetailsEmpty() {

        when(candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        anyString(),
                        anyString()))
                .thenReturn(List.of());

        List<EmploymentDetailsDto> result =
                employmentService.searchEmploymentDetails("xyz");

        assertTrue(result.isEmpty());
    }
    @Test
    void testDeleteEmploymentDetailsNotFound() {

        when(employmentRepository.findByCandidateId(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.deleteEmploymentDetails(1L));

        assertEquals(
                "Employment details not found",
                exception.getMessage());

        verify(employmentRepository, never())
                .delete(any());
    }
    @Test
    void testExperiencedPreviousCompanyRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("")
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Previous Company Name is required.",
                exception.getMessage());
    }
    @Test
    void testExperiencedPreviousDesignationRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("")
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Previous Designation is required.",
                exception.getMessage());
    }
    @Test
    void testExperiencedLastCtcRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(2.5)
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Last CTC is required.",
                exception.getMessage());
    }
    @Test
    void testExperiencedLastWorkingDayRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(2.5)
                .lastCTC(600000.0)
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Last Working Day is required.",
                exception.getMessage());
    }
    @Test
    void testExperiencedUanRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(2.5)
                .lastCTC(600000.0)
                .lastWorkingDay(LocalDate.now())
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "UAN Number is required.",
                exception.getMessage());
    }
    @Test
    void testExperiencedEmploymentStatusRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(2.5)
                .lastCTC(600000.0)
                .lastWorkingDay(LocalDate.now())
                .uanNumber("123456789012")
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Employment Status is required.",
                exception.getMessage());
    }
    @Test
    void testCurrentCompanyRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(3.0)
                .lastCTC(600000.0)
                .lastWorkingDay(LocalDate.now())
                .uanNumber("123456789012")
                .employmentStatus(EmploymentStatus.CURRENTLY_EMPLOYED)
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Current Company is required.",
                ex.getMessage());
    }
    @Test
    void testCurrentCtcRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(3.0)
                .lastCTC(600000.0)
                .lastWorkingDay(LocalDate.now())
                .uanNumber("123456789012")
                .employmentStatus(EmploymentStatus.CURRENTLY_EMPLOYED)
                .currentCompany("Infosys")
                .currentDesignation("Software Engineer")
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Current CTC is required.",
                ex.getMessage());
    }
    @Test
    void testNoticePeriodRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .previousCompanyName("ABC")
                .previousDesignation("Developer")
                .totalExperience(3.0)
                .lastCTC(600000.0)
                .lastWorkingDay(LocalDate.now())
                .uanNumber("123456789012")
                .employmentStatus(EmploymentStatus.CURRENTLY_EMPLOYED)
                .currentCompany("Infosys")
                .currentDesignation("Software Engineer")
                .currentCTC(800000.0)
                .offerLetterStatus(OfferLetterStatus.NOT_HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Notice Period is required.",
                ex.getMessage());
    }
    @Test
    void testOfferLetterStatusRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(
                        EmploymentDetailsDto.builder().build()));

        assertEquals(
                "Offer Letter Status is required.",
                ex.getMessage());
    }
    @Test
    void testOfferCompanyRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .offerLetterStatus(
                        OfferLetterStatus.HOLDING_OFFER_LETTER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Offer Company Name is required.",
                ex.getMessage());
    }
    @Test
    void testJoiningDateRequired() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .candidateType(CandidateType.FRESHER)
                .build();

        EmploymentDetailsDto dto = EmploymentDetailsDto.builder()
                .offerLetterStatus(
                        OfferLetterStatus.HOLDING_OFFER_LETTER)
                .offerCompanyName("Google")
                .offeredCTC(1200000.0)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employmentService.saveEmploymentDetails(dto));

        assertEquals(
                "Joining Date is required.",
                ex.getMessage());
    }
}