package com.verify_x.services;

import com.verify_x.dto.VerificationQueueItemDto;

import java.util.List;

public interface VerificationService {

    // Powers the "Verification" sidebar screen: candidates that still need
    // HR attention (documents pending review and/or UAN not yet verified).
    List<VerificationQueueItemDto> getVerificationQueue();
}
