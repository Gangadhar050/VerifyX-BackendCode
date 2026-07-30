package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateEducationDto;
import com.verify_x.entity.Candidate;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CandidateEducationServiceImpl implements CandidateEducationService {

    private final CandidateRepository candidateRepository;

    private byte[] convertFileToBytes(MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                return file.getBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
        return null;
    }

    private String getContentType(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            return file.getContentType();
        }
        return null;
    }

    @Override
    public CandidateEducationDto saveEducation(Long candidateId,
                                               CandidateEducationDto educationDto) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // ===========================
        // 10th Details
        // ===========================
        candidate.setTenthSchoolName(educationDto.getTenthSchoolName());
        candidate.setTenthBoard(educationDto.getTenthBoard());
        candidate.setTenthSchoolLocation(educationDto.getTenthSchoolLocation());
        candidate.setTenthRegistrationNumber(educationDto.getTenthRegistrationNumber());
        candidate.setTenthPassingYear(educationDto.getTenthPassingYear());
        candidate.setTenthPercentage(educationDto.getTenthPercentage());
        candidate.setTenthMarksCard(convertFileToBytes(educationDto.getTenthMarksCard()));
        candidate.setTenthMarksCardType(getContentType(educationDto.getTenthMarksCard()));

        // ===========================
        // PUC / 12th / Diploma
        // ===========================
        candidate.setPucInstitutionName(educationDto.getPucInstitutionName());
        candidate.setPucBoardUniversity(educationDto.getPucBoardUniversity());
        candidate.setPucStream(educationDto.getPucStream());
        candidate.setPucRegistrationNumber(educationDto.getPucRegistrationNumber());
        candidate.setPucPassingYear(educationDto.getPucPassingYear());
        candidate.setPucPercentage(educationDto.getPucPercentage());
        candidate.setPucMarksCard(convertFileToBytes(educationDto.getPucMarksCard()));
        candidate.setPucMarksCardType(getContentType(educationDto.getPucMarksCard()));

        // ===========================
        // Bachelor's Degree
        // ===========================
        candidate.setBachelorDegree(educationDto.getBachelorDegree());
        candidate.setBachelorSpecialization(educationDto.getBachelorSpecialization());
        candidate.setBachelorCollegeName(educationDto.getBachelorCollegeName());
        candidate.setBachelorUniversityName(educationDto.getBachelorUniversityName());
        candidate.setBachelorUsnNumber(educationDto.getBachelorUsnNumber());
        candidate.setBachelorStartYear(educationDto.getBachelorStartYear());
        candidate.setBachelorEndYear(educationDto.getBachelorEndYear());
        candidate.setBachelorPercentage(educationDto.getBachelorPercentage());
        candidate.setBachelorBacklogs(educationDto.getBachelorBacklogs());
        candidate.setBachelorMarksCard(convertFileToBytes(educationDto.getBachelorMarksCard()));
        candidate.setBachelorMarksCardType(getContentType(educationDto.getBachelorMarksCard()));
        candidate.setBachelorDegreeCertificate(convertFileToBytes(educationDto.getBachelorDegreeCertificate()));
        candidate.setBachelorDegreeCertificateType(getContentType(educationDto.getBachelorDegreeCertificate()));

        // ===========================
        // Master's Degree
        // ===========================
        candidate.setMasterDegree(educationDto.getMasterDegree());
        candidate.setMasterSpecialization(educationDto.getMasterSpecialization());
        candidate.setMasterCollegeName(educationDto.getMasterCollegeName());
        candidate.setMasterUniversityName(educationDto.getMasterUniversityName());
        candidate.setMasterRegistrationNumber(educationDto.getMasterRegistrationNumber());
        candidate.setMasterModeOfStudy(educationDto.getMasterModeOfStudy());
        candidate.setMasterStartYear(educationDto.getMasterStartYear());
        candidate.setMasterEndYear(educationDto.getMasterEndYear());
        candidate.setMasterPercentage(educationDto.getMasterPercentage());
        candidate.setMasterMarksCard(convertFileToBytes(educationDto.getMasterMarksCard()));
        candidate.setMasterMarksCardType(getContentType(educationDto.getMasterMarksCard()));
        candidate.setMasterConsolidatedMarksCard(convertFileToBytes(educationDto.getMasterConsolidatedMarksCard()));
        candidate.setMasterConsolidatedMarksCardType(getContentType(educationDto.getMasterConsolidatedMarksCard()));
        candidate.setMasterDegreeCertificate(convertFileToBytes(educationDto.getMasterDegreeCertificate()));
        candidate.setMasterDegreeCertificateType(getContentType(educationDto.getMasterDegreeCertificate()));

        // ===========================
        // Technical Skills
        // ===========================
        candidate.setTechnicalSkills(educationDto.getTechnicalSkills());

        candidateRepository.save(candidate);

        return educationDto;
    }

    @Override
    public CandidateEducationDto getEducation(Long candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // NOTE: File fields (tenthMarksCard, pucMarksCard, etc.) are MultipartFile
        // in this DTO, which cannot be reconstructed from stored byte[] data.
        // They are intentionally left null here. To return actual file bytes
        // for download/preview, expose separate byte[]-returning endpoints
        // (e.g. GET /education/{id}/tenth-marks-card) instead.
        return CandidateEducationDto.builder()

                // ===========================
                // 10th Details
                // ===========================
                .tenthSchoolName(candidate.getTenthSchoolName())
                .tenthBoard(candidate.getTenthBoard())
                .tenthSchoolLocation(candidate.getTenthSchoolLocation())
                .tenthRegistrationNumber(candidate.getTenthRegistrationNumber())
                .tenthPassingYear(candidate.getTenthPassingYear())
                .tenthPercentage(candidate.getTenthPercentage())

                // ===========================
                // PUC / 12th / Diploma
                // ===========================
                .pucInstitutionName(candidate.getPucInstitutionName())
                .pucBoardUniversity(candidate.getPucBoardUniversity())
                .pucStream(candidate.getPucStream())
                .pucRegistrationNumber(candidate.getPucRegistrationNumber())
                .pucPassingYear(candidate.getPucPassingYear())
                .pucPercentage(candidate.getPucPercentage())

                // ===========================
                // Bachelor's Degree
                // ===========================
                .bachelorDegree(candidate.getBachelorDegree())
                .bachelorSpecialization(candidate.getBachelorSpecialization())
                .bachelorCollegeName(candidate.getBachelorCollegeName())
                .bachelorUniversityName(candidate.getBachelorUniversityName())
                .bachelorUsnNumber(candidate.getBachelorUsnNumber())
                .bachelorStartYear(candidate.getBachelorStartYear())
                .bachelorEndYear(candidate.getBachelorEndYear())
                .bachelorPercentage(candidate.getBachelorPercentage())
                .bachelorBacklogs(candidate.getBachelorBacklogs())

                // ===========================
                // Master's Degree
                // ===========================
                .masterDegree(candidate.getMasterDegree())
                .masterSpecialization(candidate.getMasterSpecialization())
                .masterCollegeName(candidate.getMasterCollegeName())
                .masterUniversityName(candidate.getMasterUniversityName())
                .masterRegistrationNumber(candidate.getMasterRegistrationNumber())
                .masterModeOfStudy(candidate.getMasterModeOfStudy())
                .masterStartYear(candidate.getMasterStartYear())
                .masterEndYear(candidate.getMasterEndYear())
                .masterPercentage(candidate.getMasterPercentage())

                // ===========================
                // Technical Skills
                // ===========================
                .technicalSkills(candidate.getTechnicalSkills())

                .build();
    }

    @Override
    public CandidateEducationDto updateEducation(Long candidateId,
                                                 CandidateEducationDto educationDto) {
        return saveEducation(candidateId, educationDto);
    }

    @Override
    public void deleteEducation(Long candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // 10th
        candidate.setTenthSchoolName(null);
        candidate.setTenthBoard(null);
        candidate.setTenthSchoolLocation(null);
        candidate.setTenthRegistrationNumber(null);
        candidate.setTenthPassingYear(null);
        candidate.setTenthPercentage(null);
        candidate.setTenthMarksCard(null);
        candidate.setTenthMarksCardType(null);

        // PUC
        candidate.setPucInstitutionName(null);
        candidate.setPucBoardUniversity(null);
        candidate.setPucStream(null);
        candidate.setPucRegistrationNumber(null);
        candidate.setPucPassingYear(null);
        candidate.setPucPercentage(null);
        candidate.setPucMarksCard(null);
        candidate.setPucMarksCardType(null);

        // Bachelor
        candidate.setBachelorDegree(null);
        candidate.setBachelorSpecialization(null);
        candidate.setBachelorCollegeName(null);
        candidate.setBachelorUniversityName(null);
        candidate.setBachelorUsnNumber(null);
        candidate.setBachelorStartYear(null);
        candidate.setBachelorEndYear(null);
        candidate.setBachelorPercentage(null);
        candidate.setBachelorBacklogs(null);
        candidate.setBachelorMarksCard(null);
        candidate.setBachelorMarksCardType(null);
        candidate.setBachelorDegreeCertificate(null);
        candidate.setBachelorDegreeCertificateType(null);

        // Master
        candidate.setMasterDegree(null);
        candidate.setMasterSpecialization(null);
        candidate.setMasterCollegeName(null);
        candidate.setMasterUniversityName(null);
        candidate.setMasterRegistrationNumber(null);
        candidate.setMasterModeOfStudy(null);
        candidate.setMasterStartYear(null);
        candidate.setMasterEndYear(null);
        candidate.setMasterPercentage(null);
        candidate.setMasterMarksCard(null);
        candidate.setMasterMarksCardType(null);
        candidate.setMasterConsolidatedMarksCard(null);
        candidate.setMasterConsolidatedMarksCardType(null);
        candidate.setMasterDegreeCertificate(null);
        candidate.setMasterDegreeCertificateType(null);

        candidate.setTechnicalSkills(null);

        candidateRepository.save(candidate);
    }
}