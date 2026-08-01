package com.verify_x.services;

import com.verify_x.dto.*;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;

import java.util.List;

public interface CandidateManagementService {

//    PagedResponse<CandidateSummaryDto> getAllCandidates(
//            String keyword,
//            CandidateType candidateType,
//            ApplicationStatus applicationStatus,
//            int page,
//            int size
//    );
List<CandidateSummaryDto> getAllCandidates();

    List<CandidateSummaryDto> searchCandidates(String keyword);

    CandidateDetailsDto getCandidateDetails(Long candidateId);

    // HR-side "+ Add Candidate"
    CandidateSummaryDto createCandidate(UserRegistrationDto dto);

    void deleteCandidate(Long candidateId);

    void verifyUan(Long candidateId, String verifiedBy);

    void updateApplicationStatus(Long candidateId,
                                 ApplicationStatusUpdateDto dto,
                                 String reviewedBy);
}
