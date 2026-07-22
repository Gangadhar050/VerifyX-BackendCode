package com.verify_x.services;

import com.verify_x.dto.CandidateEducationDto;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.entity.Candidate;

import java.util.List;

public interface CandidateService {

    CandidateProfileDto getCandidateProfile(Long userId);

    CandidateProfileDto saveCandidateProfile(Long userId,
                                             CandidateProfileDto dto);

    CandidateProfileDto updateCandidateProfile(Long userId,
                                               CandidateProfileDto dto);

    void saveCandidateProfile(Candidate savedUser);


    // Education
    void saveEducation(CandidateEducationDto dto);

    void updateEducation(CandidateEducationDto dto);

    CandidateEducationDto getEducationByCandidateId(Long candidateId);

    CandidateEducationDto getEducationByEmail(String email);

    List<CandidateEducationDto> searchEducation(String keyword);

    void deleteEducation(Long candidateId);
}
