package com.verify_x.serviceImpl;

import com.verify_x.dto.HRDocumentDto;
import com.verify_x.dto.PagedResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import com.verify_x.repository.CandidateDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HRDocumentServiceImpl Test Suite")
class HRDocumentServiceImplTest {

    @Mock
    private CandidateDocumentRepository candidateDocumentRepository;

    @InjectMocks
    private HRDocumentServiceImpl hrDocumentService;

    private Candidate candidate1;
    private Candidate candidate2;
    private CandidateDocument doc1;
    private CandidateDocument doc2;
    private CandidateDocument doc3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidate1 = new Candidate();
        candidate1.setId(1L);
        candidate1.setUsername("alice");
        candidate1.setEmail("alice@example.com");

        candidate2 = new Candidate();
        candidate2.setId(2L);
        candidate2.setUsername("bob");
        candidate2.setEmail("bob@example.com");

        doc1 = new CandidateDocument();
        doc1.setId(10L);
        doc1.setCandidate(candidate1);
        doc1.setFileName("resume-alice.pdf");
        doc1.setDocumentType(DocumentType.RESUME);
        doc1.setStatus(DocumentStatus.VERIFIED);
        doc1.setUploadedAt(LocalDateTime.now().minusDays(1));

        doc2 = new CandidateDocument();
        doc2.setId(11L);
        doc2.setCandidate(candidate2);
        doc2.setFileName("pan-bob.pdf");
        doc2.setDocumentType(DocumentType.PAN_CARD);
        doc2.setStatus(DocumentStatus.PENDING);
        doc2.setUploadedAt(LocalDateTime.now().minusHours(2));

        doc3 = new CandidateDocument();
        doc3.setId(12L);
        doc3.setCandidate(candidate1);
        doc3.setFileName("offer-alice.pdf");
        doc3.setDocumentType(DocumentType.OFFER_LETTER);
        doc3.setStatus(DocumentStatus.REJECTED);
        doc3.setUploadedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllDocuments returns paged, sorted results when no filters")
    void testGetAllDocumentsNoFiltersPagedSorted() {
        when(candidateDocumentRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        PagedResponse<HRDocumentDto> page0 = hrDocumentService.getAllDocuments(null, null, null, 0, 2);

        assertNotNull(page0);
        assertEquals(0, page0.getPage());
        assertEquals(2, page0.getSize());
        assertEquals(3, page0.getTotalElements());
        assertEquals(2, page0.getTotalPages());
        // docs should be sorted by uploadedAt desc: doc3, doc2
        assertEquals(2, page0.getContent().size());
        assertEquals(doc3.getId(), page0.getContent().get(0).getDocumentId());
        assertEquals(doc2.getId(), page0.getContent().get(1).getDocumentId());

        // second page
        PagedResponse<HRDocumentDto> page1 = hrDocumentService.getAllDocuments(null, null, null, 1, 2);
        assertEquals(1, page1.getPage());
        assertEquals(1, page1.getContent().size());
        assertEquals(doc1.getId(), page1.getContent().get(0).getDocumentId());
    }

    @Test
    @DisplayName("getAllDocuments filters by status and document type")
    void testGetAllDocumentsFiltersStatusAndType() {
        when(candidateDocumentRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        PagedResponse<HRDocumentDto> res = hrDocumentService.getAllDocuments(null, DocumentStatus.PENDING, DocumentType.PAN_CARD, 0, 10);

        assertEquals(1, res.getTotalElements());
        assertEquals(DocumentType.PAN_CARD, res.getContent().get(0).getDocumentType());
        assertEquals(DocumentStatus.PENDING, res.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("getAllDocuments keyword matches username, email or filename (case-insensitive)")
    void testGetAllDocumentsKeywordSearch() {
        when(candidateDocumentRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        // match on username 'alice'
        PagedResponse<HRDocumentDto> r1 = hrDocumentService.getAllDocuments("Alice", null, null, 0, 10);
        assertEquals(2, r1.getTotalElements());

        // match on email 'bob@example'
        PagedResponse<HRDocumentDto> r2 = hrDocumentService.getAllDocuments("bob@EXAMPLE", null, null, 0, 10);
        assertEquals(1, r2.getTotalElements());

        // match on filename
        PagedResponse<HRDocumentDto> r3 = hrDocumentService.getAllDocuments("offer-ALICE", null, null, 0, 10);
        assertEquals(1, r3.getTotalElements());
    }

    @Test
    @DisplayName("getAllDocuments handles pagination bounds gracefully")
    void testGetAllDocumentsPaginationBounds() {
        when(candidateDocumentRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        // page beyond range
        PagedResponse<HRDocumentDto> r = hrDocumentService.getAllDocuments(null, null, null, 5, 2);
        assertNotNull(r);
        assertTrue(r.getContent().isEmpty());
        assertEquals(3, r.getTotalElements());
    }

    @Test
    @DisplayName("getAllDocuments uses safe defaults for negative page and size")
    void testGetAllDocumentsNegativePageOrSize() {
        when(candidateDocumentRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        // negative page -> treated as 0
        PagedResponse<HRDocumentDto> r1 = hrDocumentService.getAllDocuments(null, null, null, -1, 2);
        assertEquals(0, r1.getPage());

        // size <=0 -> default 10
        PagedResponse<HRDocumentDto> r2 = hrDocumentService.getAllDocuments(null, null, null, 0, 0);
        assertEquals(10, r2.getSize());
        assertEquals(3, r2.getContent().size());
    }

    @Test
    @DisplayName("getAllDocuments returns empty when repository empty")
    void testGetAllDocumentsEmptyRepository() {
        when(candidateDocumentRepository.findAll()).thenReturn(Collections.emptyList());

        PagedResponse<HRDocumentDto> r = hrDocumentService.getAllDocuments(null, null, null, 0, 10);
        assertNotNull(r);
        assertTrue(r.getContent().isEmpty());
        assertEquals(0, r.getTotalElements());
        assertEquals(0, r.getTotalPages());
    }
}
