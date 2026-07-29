package com.verify_x.dto;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationQueueItemDto {

    private Long candidateId;
    private String candidateName;
    private String email;
    private CandidateType candidateType;
    private ApplicationStatus applicationStatus;

    private long pendingDocumentsCount;
    private long rejectedDocumentsCount;
    private long verifiedDocumentsCount;

    private boolean uanRequired;
    private boolean uanVerified;

    // true when there is nothing left for HR to check (all docs verified + UAN ok if required)
    private boolean readyForDecision;
}
