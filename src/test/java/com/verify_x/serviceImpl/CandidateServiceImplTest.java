package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.CandidateType;
import com.verify_x.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    private CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        candidateService = new CandidateServiceImpl(candidateRepository);
    }

    @Test
    void testGetCandidateProfileSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .email("john@test.com")
                .phoneNumber("9876543210")
                .appliedRole("Java Developer")
                .candidateType(CandidateType.FRESHER)
                .address("Bangalore")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        CandidateProfileDto dto =
                candidateService.getCandidateProfile(1L);

        assertNotNull(dto);
        assertEquals("John", dto.getUsername());
        assertEquals("john@test.com", dto.getEmail());
        assertEquals("Bangalore", dto.getAddress());
    }

    @Test
    void testGetCandidateProfileUserNotFound() {

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> candidateService.getCandidateProfile(1L));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testSaveCandidateProfileSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .email("john@test.com")
                .build();

        CandidateProfileDto dto = CandidateProfileDto.builder()
                .username("John")
                .email("john@test.com")
                .phoneNumber("9999999999")
                .address("Mysore")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .appliedRole("Developer")
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateRepository.existsByPanNumber(dto.getPanNumber()))
                .thenReturn(false);

        when(candidateRepository.existsByAadhaarNumber(dto.getAadhaarNumber()))
                .thenReturn(false);

        CandidateProfileDto result =
                candidateService.saveCandidateProfile(1L, dto);

        assertNotNull(result);
        assertEquals(dto.getUsername(), result.getUsername());

        verify(candidateRepository, times(2))
                .save(any(Candidate.class));
    }

    @Test
    void testSaveCandidateProfilePanAlreadyExists() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        CandidateProfileDto dto = CandidateProfileDto.builder()
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateRepository.existsByPanNumber(dto.getPanNumber()))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> candidateService.saveCandidateProfile(1L, dto));

        assertEquals("PAN Number already exists.", exception.getMessage());

        verify(candidateRepository, never()).save(any());
    }

    @Test
    void testSaveCandidateProfileAadhaarAlreadyExists() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .build();

        CandidateProfileDto dto = CandidateProfileDto.builder()
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        when(candidateRepository.existsByPanNumber(anyString()))
                .thenReturn(false);

        when(candidateRepository.existsByAadhaarNumber(anyString()))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> candidateService.saveCandidateProfile(1L, dto));

        assertEquals("Aadhaar Number already exists.", exception.getMessage());

        verify(candidateRepository, never()).save(any());
    }

    @Test
    void testUpdateCandidateProfileSuccess() {

        Candidate candidate = Candidate.builder()
                .id(1L)
                .username("John")
                .build();

        CandidateProfileDto dto = CandidateProfileDto.builder()
                .username("Updated")
                .email("updated@test.com")
                .phoneNumber("9999999999")
                .address("Delhi")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .appliedRole("Developer")
                .candidateType(CandidateType.EXPERIENCED)
                .build();

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.of(candidate));

        CandidateProfileDto result =
                candidateService.updateCandidateProfile(1L, dto);

        assertEquals("Updated", result.getUsername());

        verify(candidateRepository, times(2))
                .save(any(Candidate.class));
    }

    @Test
    void testUpdateCandidateProfileUserNotFound() {

        when(candidateRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> candidateService.updateCandidateProfile(1L,
                                CandidateProfileDto.builder().build()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testSaveCandidateEntitySuccess() {

        Candidate candidate = Candidate.builder()
                .id(10L)
                .username("John")
                .build();

        candidateService.saveCandidateProfile(candidate);

        verify(candidateRepository).save(candidate);
    }

}