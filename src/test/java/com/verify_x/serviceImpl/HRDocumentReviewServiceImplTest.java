package com.verify_x.serviceImpl;

import com.verify_x.dto.HRDocumentReviewDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Employment;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import com.verify_x.enums.OfferLetterStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EmploymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HRDocumentReviewServiceImpl Test Suite")
class HRDocumentReviewServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @InjectMocks
    private HRDocumentReviewServiceImpl hrDocumentReviewService;

    private Candidate candidate;
    private CandidateDocument panDoc;
    private CandidateDocument resumeDoc;
    private Employment employment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidate = new Candidate();
        candidate.setId(10L);
        candidate.setUsername("hr_user");
        candidate.setEmail("hr@example.com");
        candidate.setCandidateType(CandidateType.FRESHER);
        candidate.setApplicationStatus(ApplicationStatus.PENDING_VERIFICATION);

        panDoc = new CandidateDocument();
        panDoc.setId(100L);
        panDoc.setCandidate(candidate);
        panDoc.setDocumentType(DocumentType.PAN_CARD);
        panDoc.setFileName("pan.pdf");
        panDoc.setStatus(DocumentStatus.VERIFIED);
        panDoc.setUploadedAt(LocalDateTime.now());

        resumeDoc = new CandidateDocument();
        resumeDoc.setId(101L);
        resumeDoc.setCandidate(candidate);
        resumeDoc.setDocumentType(DocumentType.RESUME);
        resumeDoc.setFileName("resume.pdf");
        resumeDoc.setStatus(DocumentStatus.PENDING);
        resumeDoc.setUploadedAt(LocalDateTime.now());

        employment = new Employment();
        employment.setId(200L);
        employment.setCandidate(candidate);
        employment.setUanNumber("UAN123");
        employment.setUanVerified(true);
        employment.setUanVerifiedBy("HR1");
        employment.setUanVerifiedAt(LocalDateTime.now());
        employment.setOfferLetterStatus(OfferLetterStatus.HOLDING_OFFER_LETTER);
    }

    @Test
    @DisplayName("getCandidateDocuments throws when candidate not found")
    void testGetCandidateDocumentsCandidateNotFound() {
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hrDocumentReviewService.getCandidateDocuments(999L));
    }

    @Test
    @DisplayName("getCandidateDocuments returns proper counts and critical docs without employment")
    void testGetCandidateDocumentsWithoutEmployment() {
        when(candidateRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(employmentRepository.findByCandidate(candidate)).thenReturn(Optional.empty());
        when(candidateDocumentRepository.findByCandidate(candidate)).thenReturn(Arrays.asList(panDoc, resumeDoc));

        HRDocumentReviewDto dto = hrDocumentReviewService.getCandidateDocuments(candidate.getId());

        assertNotNull(dto);
        assertEquals(candidate.getUsername(), dto.getCandidateName());
        assertEquals(candidate.getEmail(), dto.getEmail());
        assertEquals(2, dto.getVerifiedDocuments() + dto.getPendingDocuments());
        assertEquals(1, dto.getVerifiedDocuments());
        assertEquals(1, dto.getPendingDocuments());
        assertEquals("No", dto.getHoldingOfferLetter());
        assertNull(dto.getUanVerification());

        // critical documents should include PAN_CARD
        assertTrue(dto.getCriticalDocuments().stream().anyMatch(c -> c.getDocumentType() == DocumentType.PAN_CARD));
    }

    @Test
    @DisplayName("getCandidateDocuments includes employment info and holding offer letter")
    void testGetCandidateDocumentsWithEmployment() {
        when(candidateRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(employmentRepository.findByCandidate(candidate)).thenReturn(Optional.of(employment));
        when(candidateDocumentRepository.findByCandidate(candidate)).thenReturn(Collections.emptyList());

        HRDocumentReviewDto dto = hrDocumentReviewService.getCandidateDocuments(candidate.getId());

        assertNotNull(dto);
        assertEquals("Yes", dto.getHoldingOfferLetter());
        assertTrue(dto.isUanVerified());
        assertNotNull(dto.getUanVerification());
        assertTrue(dto.getUanVerification().getStatus().contains("Verified"));
        assertTrue(dto.getUanVerification().getVerifiedMessage().contains(employment.getUanVerifiedBy()));
    }

    @Test
    @DisplayName("viewDocument returns resource when data present")
    void testViewDocumentSuccess() {
        byte[] data = "hello".getBytes();
        panDoc.setDocumentData(data);
        when(candidateDocumentRepository.findById(panDoc.getId())).thenReturn(Optional.of(panDoc));

        Resource res = hrDocumentReviewService.viewDocument(panDoc.getId());
        assertNotNull(res);
        assertTrue(res instanceof ByteArrayResource);
        assertArrayEquals(data, ((ByteArrayResource) res).getByteArray());
    }

    @Test
    @DisplayName("viewDocument throws when data missing")
    void testViewDocumentMissingData() {
        panDoc.setDocumentData(null);
        when(candidateDocumentRepository.findById(panDoc.getId())).thenReturn(Optional.of(panDoc));
        assertThrows(ResourceNotFoundException.class, () -> hrDocumentReviewService.viewDocument(panDoc.getId()));
    }

    @Test
    @DisplayName("getDocumentEntity throws when not found")
    void testGetDocumentEntityNotFound() {
        when(candidateDocumentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hrDocumentReviewService.getDocumentEntity(999L));
    }

    @Test
    @DisplayName("verifyDocument updates document and candidate status for fresher")
    void testVerifyDocumentUpdatesStatusesForFresher() {
        // arrange: single document pending -> verify
        CandidateDocument doc = new CandidateDocument();
        doc.setId(300L);
        doc.setCandidate(candidate);
        doc.setStatus(DocumentStatus.PENDING);
        doc.setDocumentType(DocumentType.RESUME);

        when(candidateDocumentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));
        when(candidateDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // after saving, repository should return only verified documents
        when(candidateDocumentRepository.findByCandidate(candidate)).thenReturn(Arrays.asList(doc));
        when(candidateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        hrDocumentReviewService.verifyDocument(doc.getId());

        assertEquals(DocumentStatus.VERIFIED, doc.getStatus());
        assertNull(doc.getRejectionReason());
        verify(candidateDocumentRepository, times(1)).save(doc);
        verify(candidateRepository, times(1)).save(candidate);
    }

    @Test
    @DisplayName("rejectDocument throws BadRequest when reason blank")
    void testRejectDocumentBadRequestWhenReasonBlank() {
        CandidateDocument doc = new CandidateDocument();
        doc.setId(400L);
        doc.setCandidate(candidate);
        when(candidateDocumentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        assertThrows(BadRequestException.class, () -> hrDocumentReviewService.rejectDocument(doc.getId(), ""));
    }

    @Test
    @DisplayName("rejectDocument sets rejected and updates candidate status")
    void testRejectDocumentSetsRejectedAndUpdatesCandidate() {
        CandidateDocument doc = new CandidateDocument();
        doc.setId(500L);
        doc.setCandidate(candidate);
        doc.setStatus(DocumentStatus.PENDING);

        when(candidateDocumentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));
        when(candidateDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // repository returns this rejected doc
        when(candidateDocumentRepository.findByCandidate(candidate)).thenReturn(Arrays.asList(doc));
        when(candidateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        hrDocumentReviewService.rejectDocument(doc.getId(), "Invalid PAN");

        assertEquals(DocumentStatus.REJECTED, doc.getStatus());
        assertEquals("Invalid PAN", doc.getRejectionReason());
        verify(candidateDocumentRepository, times(1)).save(doc);
        verify(candidateRepository, times(1)).save(candidate);
    }
}
