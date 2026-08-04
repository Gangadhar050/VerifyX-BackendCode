package com.verify_x.serviceImpl;

import com.verify_x.dto.HRCandidateDashboardDto;
import com.verify_x.dto.HRDashboardResponseDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HRDashboardServiceImpl Test Suite")
class HRDashboardServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @InjectMocks
    private HRDashboardServiceImpl hrDashboardService;

    private Candidate fresherCandidate;
    private Candidate experiencedCandidate;
    private CandidateDocument candidateDocument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test candidates
        fresherCandidate = new Candidate();
        fresherCandidate.setId(1L);
        fresherCandidate.setUsername("john_fresher");
        fresherCandidate.setEmail("john@example.com");
        fresherCandidate.setCandidateType(CandidateType.FRESHER);
        fresherCandidate.setApplicationStatus(ApplicationStatus.PENDING_VERIFICATION);
        fresherCandidate.setTechnicalSkills(Arrays.asList(TechnicalSkill.JAVA, TechnicalSkill.SPRING_BOOT));
        fresherCandidate.setAppliedRole("Backend Developer");

        experiencedCandidate = new Candidate();
        experiencedCandidate.setId(2L);
        experiencedCandidate.setUsername("jane_experienced");
        experiencedCandidate.setEmail("jane@example.com");
        experiencedCandidate.setCandidateType(CandidateType.EXPERIENCED);
        experiencedCandidate.setApplicationStatus(ApplicationStatus.APPROVED);
        experiencedCandidate.setTechnicalSkills(Arrays.asList(TechnicalSkill.PYTHON, TechnicalSkill.MONGODB));
        experiencedCandidate.setAppliedRole("Senior Developer");

        // Create test document
        candidateDocument = new CandidateDocument();
        candidateDocument.setId(1L);
        candidateDocument.setCandidate(fresherCandidate);
        candidateDocument.setStatus(DocumentStatus.VERIFIED);
        candidateDocument.setUploadedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test getDashboardReport returns correct counts for all candidates")
    void testGetDashboardReportWithMultipleCandidates() {
        // Arrange
        List<Candidate> candidates = Arrays.asList(
                fresherCandidate,
                experiencedCandidate
        );

        when(candidateRepository.count()).thenReturn(2L);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION)).thenReturn(1L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.APPROVED)).thenReturn(1L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED)).thenReturn(0L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getTotal());
        assertEquals(1L, response.getFreshers());
        assertEquals(1L, response.getExperienced());
        assertEquals(1L, response.getPending());
        assertEquals(1L, response.getApproved());
        assertEquals(0L, response.getRejected());

        verify(candidateRepository, times(1)).count();
        verify(candidateRepository, times(2)).findAll();
        verify(candidateRepository, times(1)).countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION);
        verify(candidateRepository, times(1)).countByApplicationStatus(ApplicationStatus.APPROVED);
        verify(candidateRepository, times(1)).countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED);
    }

    @Test
    @DisplayName("Test getDashboardReport with empty database")
    void testGetDashboardReportWithNoCandidates() {
        // Arrange
        when(candidateRepository.count()).thenReturn(0L);
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION)).thenReturn(0L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.APPROVED)).thenReturn(0L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED)).thenReturn(0L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertNotNull(response);
        assertEquals(0L, response.getTotal());
        assertEquals(0L, response.getFreshers());
        assertEquals(0L, response.getExperienced());
        assertEquals(0L, response.getPending());
        assertEquals(0L, response.getApproved());
        assertEquals(0L, response.getRejected());
    }

    @Test
    @DisplayName("Test getDashboardReport counts only freshers correctly")
    void testGetDashboardReportCountsFreshersOnly() {
        // Arrange
        Candidate fresher2 = new Candidate();
        fresher2.setId(3L);
        fresher2.setUsername("mike_fresher");
        fresher2.setCandidateType(CandidateType.FRESHER);

        List<Candidate> candidates = Arrays.asList(fresherCandidate, fresher2, experiencedCandidate);

        when(candidateRepository.count()).thenReturn(3L);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION)).thenReturn(0L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.APPROVED)).thenReturn(0L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED)).thenReturn(0L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertEquals(3L, response.getTotal());
        assertEquals(2L, response.getFreshers());
        assertEquals(1L, response.getExperienced());
    }

    @Test
    @DisplayName("Test getDashboardReport counts application statuses correctly")
    void testGetDashboardReportCountsStatusesCorrectly() {
        // Arrange
        when(candidateRepository.count()).thenReturn(10L);
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(fresherCandidate, experiencedCandidate));
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION)).thenReturn(5L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.APPROVED)).thenReturn(3L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED)).thenReturn(2L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertEquals(5L, response.getPending());
        assertEquals(3L, response.getApproved());
        assertEquals(2L, response.getRejected());
    }

    @Test
    @DisplayName("Test getCandidates returns list of candidates with proper DTOs")
    void testGetCandidatesReturnsAllCandidates() {
        // Arrange
        List<Candidate> candidates = Arrays.asList(fresherCandidate, experiencedCandidate);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(fresherCandidate))
                .thenReturn(Arrays.asList(candidateDocument));
        when(candidateDocumentRepository.findByCandidate(experiencedCandidate))
                .thenReturn(Collections.emptyList());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("john_fresher", result.get(0).getUsername());
        assertEquals("jane_experienced", result.get(1).getUsername());

        verify(candidateRepository, times(1)).findAll();
        verify(candidateDocumentRepository, times(2)).findByCandidate(any());
    }

    @Test
    @DisplayName("Test getCandidates with empty database")
    void testGetCandidatesEmptyDatabase() {
        // Arrange
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Test getCandidates correctly maps candidate to DTO")
    void testGetCandidatesDtoMapping() {
        // Arrange
        List<Candidate> candidates = Arrays.asList(fresherCandidate);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(fresherCandidate))
                .thenReturn(Arrays.asList(candidateDocument));

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        HRCandidateDashboardDto dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("john_fresher", dto.getUsername());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("FRESHER", dto.getCandidateType());
        assertEquals("VERIFIED", dto.getStatus());
        assertEquals("Backend Developer", dto.getAppliedRole());
        assertNotNull(dto.getTechnicalSkills());
    }

    @Test
    @DisplayName("Test getCandidates sets status to Draft when no documents exist")
    void testGetCandidatesStatusDraftWhenNoDocuments() {
        // Arrange
        experiencedCandidate.setId(2L);
        List<Candidate> candidates = Arrays.asList(experiencedCandidate);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(experiencedCandidate))
                .thenReturn(Collections.emptyList());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Draft", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test getCandidates uses latest document for status")
    void testGetCandidatesUsesLatestDocumentForStatus() {
        // Arrange
        CandidateDocument oldDoc = new CandidateDocument();
        oldDoc.setStatus(DocumentStatus.PENDING);
        oldDoc.setUploadedAt(LocalDateTime.now().minusDays(1));

        CandidateDocument newDoc = new CandidateDocument();
        newDoc.setStatus(DocumentStatus.VERIFIED);
        newDoc.setUploadedAt(LocalDateTime.now());

        List<Candidate> candidates = Arrays.asList(fresherCandidate);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(fresherCandidate))
                .thenReturn(Arrays.asList(oldDoc, newDoc));

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("VERIFIED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test getCandidates handles null values gracefully")
    void testGetCandidatesHandlesNullValues() {
        // Arrange
        Candidate candidateWithNulls = new Candidate();
        candidateWithNulls.setId(5L);
        candidateWithNulls.setUsername("test_user");
        candidateWithNulls.setEmail("test@example.com");
        candidateWithNulls.setCandidateType(CandidateType.FRESHER);
        candidateWithNulls.setTechnicalSkills(null);
        candidateWithNulls.setAppliedRole(null);

        List<Candidate> candidates = Arrays.asList(candidateWithNulls);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(candidateWithNulls))
                .thenReturn(Collections.emptyList());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("test_user", result.get(0).getUsername());
        assertEquals("Draft", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test getCandidates preserves technical skills in DTO")
    void testGetCandidatesPreservesTechnicalSkills() {
        // Arrange
        List<TechnicalSkill> skills = Arrays.asList(TechnicalSkill.JAVA, TechnicalSkill.SPRING_BOOT, TechnicalSkill.REACT_JS);
        fresherCandidate.setTechnicalSkills(skills);
        
        List<Candidate> candidates = Arrays.asList(fresherCandidate);
        when(candidateRepository.findAll()).thenReturn(candidates);
        when(candidateDocumentRepository.findByCandidate(fresherCandidate))
                .thenReturn(new ArrayList<>());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getTechnicalSkills().size());
        assertTrue(result.get(0).getTechnicalSkills().contains(TechnicalSkill.JAVA));
        assertTrue(result.get(0).getTechnicalSkills().contains(TechnicalSkill.SPRING_BOOT));
        assertTrue(result.get(0).getTechnicalSkills().contains(TechnicalSkill.REACT_JS));
    }

    @Test
    @DisplayName("Test getDashboardReport returns non-null DTO")
    void testGetDashboardReportReturnsDtoNotNull() {
        // Arrange
        when(candidateRepository.count()).thenReturn(0L);
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());
        when(candidateRepository.countByApplicationStatus(any())).thenReturn(0L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertNotNull(response, "Dashboard response should not be null");
    }

    @Test
    @DisplayName("Test getCandidates returns list not null")
    void testGetCandidatesReturnsListNotNull() {
        // Arrange
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<HRCandidateDashboardDto> result = hrDashboardService.getCandidates();

        // Assert
        assertNotNull(result, "Candidates list should not be null");
    }

    @Test
    @DisplayName("Test getDashboardReport with mixed candidate types and statuses")
    void testGetDashboardReportComplexScenario() {
        // Arrange
        Candidate candidate3 = new Candidate();
        candidate3.setId(3L);
        candidate3.setCandidateType(CandidateType.FRESHER);
        candidate3.setApplicationStatus(ApplicationStatus.APPROVED);

        Candidate candidate4 = new Candidate();
        candidate4.setId(4L);
        candidate4.setCandidateType(CandidateType.EXPERIENCED);
        candidate4.setApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED);

        List<Candidate> allCandidates = Arrays.asList(fresherCandidate, experiencedCandidate, candidate3, candidate4);

        when(candidateRepository.count()).thenReturn(4L);
        when(candidateRepository.findAll()).thenReturn(allCandidates);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.PENDING_VERIFICATION)).thenReturn(1L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.APPROVED)).thenReturn(2L);
        when(candidateRepository.countByApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED)).thenReturn(1L);

        // Act
        HRDashboardResponseDto response = hrDashboardService.getDashboardReport();

        // Assert
        assertEquals(4L, response.getTotal());
        assertEquals(2L, response.getFreshers());
        assertEquals(2L, response.getExperienced());
        assertEquals(1L, response.getPending());
        assertEquals(2L, response.getApproved());
        assertEquals(1L, response.getRejected());
    }
}
