package com.verify_x.services;

import com.verify_x.dto.ApplicationStatusUpdateDto;
import com.verify_x.dto.CandidateDetailsDto;
import com.verify_x.dto.CandidateSummaryDto;

import java.util.List;

public interface CandidateManagementService {

    List<CandidateSummaryDto> getAllCandidates();

    CandidateDetailsDto getCandidateDetails(Long candidateId);

    void deleteCandidate(Long candidateId);

    void verifyUan(Long candidateId, String verifiedBy);

    void updateApplicationStatus(Long candidateId, ApplicationStatusUpdateDto dto, String reviewedBy);
}
