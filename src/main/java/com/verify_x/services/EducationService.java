package com.verify_x.services;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.enums.EducationDocumentType;
import org.springframework.core.io.Resource;

public interface EducationService {

    EducationResponse saveEducation(EducationRequest request);

    EducationResponse updateEducation(EducationRequest request);

    EducationResponse getMyEducation();

    EducationResponse getEducationByCandidateId(Long candidateId);

    Resource viewDocument(Long educationId, EducationDocumentType documentType);

    void deleteEducation();
}