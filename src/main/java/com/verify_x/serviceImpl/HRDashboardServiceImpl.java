package com.verify_x.serviceImpl;

import com.verify_x.dto.HRDashboardResponseDto;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.HRDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HRDashboardServiceImpl implements HRDashboardService {

    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;

    @Override
    public HRDashboardResponseDto getDashboardReport() {

        long total = candidateRepository.count();

        long freshers = candidateRepository.findAll()
                .stream()
                .filter(candidate ->
                        candidate.getCandidateType() == CandidateType.FRESHER)
                .count();

        long experienced = candidateRepository.findAll()
                .stream()
                .filter(candidate ->
                        candidate.getCandidateType() == CandidateType.EXPERIENCED)
                .count();

        long pending = candidateDocumentRepository
                .findByStatus(DocumentStatus.PENDING)
                .size();

        long approved = candidateDocumentRepository
                .findByStatus(DocumentStatus.VERIFIED)
                .size();

        long rejected = candidateDocumentRepository
                .findByStatus(DocumentStatus.REJECTED)
                .size();

        return new HRDashboardResponseDto(
                total,
                freshers,
                experienced,
                pending,
                approved,
                rejected
        );
    }

    @Override
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }
}