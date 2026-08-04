package com.verify_x.serviceImpl;

import com.verify_x.dto.ReportResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("ReportServiceImpl Test Suite")
class ReportServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Candidate cand1;
    private Candidate cand2;
    private CandidateDocument doc1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cand1 = new Candidate();
        cand1.setId(1L);
        cand1.setUsername("Alice Smith");
        cand1.setEmail("alice@example.com");
        cand1.setPhoneNumber("1234567890");
        cand1.setAppliedRole("Developer");
        cand1.setCandidateType(CandidateType.FRESHER);

        cand2 = new Candidate();
        cand2.setId(2L);
        cand2.setUsername("Bob Jones");
        cand2.setEmail("bob@example.com");
        cand2.setPhoneNumber("0987654321");
        cand2.setAppliedRole("Tester");
        cand2.setCandidateType(CandidateType.EXPERIENCED);

        doc1 = new CandidateDocument();
        doc1.setId(10L);
        doc1.setCandidate(cand1);
        doc1.setStatus(DocumentStatus.VERIFIED);
        doc1.setFileName("pan.pdf");
    }

    @Test
    @DisplayName("getReports returns PENDING when no documents")
    void testGetReportsPendingWhenNoDocuments() {
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(cand1));
        when(candidateDocumentRepository.findByCandidateId(cand1.getId())).thenReturn(Collections.emptyList());

        List<ReportResponse> reports = (List<ReportResponse>) (List<?>) reportService.getReports();

        assertNotNull(reports);
        assertEquals(1, reports.size());
        ReportResponse r = reports.get(0);
        assertEquals("PENDING", r.getStatus());
    }

    @Test
    @DisplayName("getReports uses first document status when present")
    void testGetReportsUsesFirstDocumentStatus() {
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(cand1));
        when(candidateDocumentRepository.findByCandidateId(cand1.getId())).thenReturn(Arrays.asList(doc1));

        List<ReportResponse> reports = (List<ReportResponse>) (List<?>) reportService.getReports();

        assertNotNull(reports);
        assertEquals(1, reports.size());
        ReportResponse r = reports.get(0);
        assertEquals(DocumentStatus.VERIFIED.name(), r.getStatus());
    }

    @Test
    @DisplayName("exportCsv produces CSV with header and rows")
    void testExportCsvProducesCsv() {
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(cand1, cand2));
        when(candidateDocumentRepository.findByCandidateId(cand1.getId())).thenReturn(Arrays.asList(doc1));
        when(candidateDocumentRepository.findByCandidateId(cand2.getId())).thenReturn(Collections.emptyList());

        byte[] bytes = reportService.exportCsv();
        assertNotNull(bytes);
        String csv = new String(bytes, StandardCharsets.UTF_8);

        // header
        assertTrue(csv.contains("Candidate ID,Full Name,Candidate Type,Status,Applied Role,Email,Phone Number"));
        // row for cand1 and cand2
        assertTrue(csv.contains("Alice Smith"));
        assertTrue(csv.contains("Bob Jones"));
        // cand1 should show VERIFIED status
        assertTrue(csv.contains(DocumentStatus.VERIFIED.name()));
        // cand2 should show PENDING
        assertTrue(csv.contains("PENDING"));
    }

    @Test
    @DisplayName("getReports handles empty candidate list")
    void testGetReportsEmpty() {
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());
        List<?> reports = reportService.getReports();
        assertNotNull(reports);
        assertTrue(reports.isEmpty());
    }
}
