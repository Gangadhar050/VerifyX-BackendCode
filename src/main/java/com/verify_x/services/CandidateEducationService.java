package com.verify_x.services;

import com.verify_x.dto.CandidateEducationDto;

public interface CandidateEducationService {

    /**
     * Save Candidate Education Details
     */
    CandidateEducationDto saveEducation(Long candidateId,
                                        CandidateEducationDto educationDto);

    /**
     * Get Candidate Education Details
     */
    CandidateEducationDto getEducation(Long candidateId);

    /**
     * Update Candidate Education Details
     */
    CandidateEducationDto updateEducation(Long candidateId,
                                          CandidateEducationDto educationDto);

    /**
     * Delete Candidate Education Details
     */
    void deleteEducation(Long candidateId);
}