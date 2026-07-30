package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateEducationDto;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.entity.Candidate;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateServiceImpl implements CandidateService {

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

    // NOTE: File fields are MultipartFile in this DTO and cannot be reconstructed
    // from stored byte[] data. They are intentionally omitted (left null) here.
    // Use a dedicated download endpoint (returning byte[]) to serve the actual files.
    private CandidateEducationDto mapToEducationDto(Candidate candidate) {

        return CandidateEducationDto.builder()

                // 10th
                .tenthSchoolName(candidate.getTenthSchoolName())
                .tenthBoard(candidate.getTenthBoard())
                .tenthSchoolLocation(candidate.getTenthSchoolLocation())
                .tenthRegistrationNumber(candidate.getTenthRegistrationNumber())
                .tenthPassingYear(candidate.getTenthPassingYear())
                .tenthPercentage(candidate.getTenthPercentage())

                // PUC
                .pucInstitutionName(candidate.getPucInstitutionName())
                .pucBoardUniversity(candidate.getPucBoardUniversity())
                .pucStream(candidate.getPucStream())
                .pucRegistrationNumber(candidate.getPucRegistrationNumber())
                .pucPassingYear(candidate.getPucPassingYear())
                .pucPercentage(candidate.getPucPercentage())

                // Bachelor
                .bachelorDegree(candidate.getBachelorDegree())
                .bachelorSpecialization(candidate.getBachelorSpecialization())
                .bachelorCollegeName(candidate.getBachelorCollegeName())
                .bachelorUniversityName(candidate.getBachelorUniversityName())
                .bachelorUsnNumber(candidate.getBachelorUsnNumber())
                .bachelorStartYear(candidate.getBachelorStartYear())
                .bachelorEndYear(candidate.getBachelorEndYear())
                .bachelorPercentage(candidate.getBachelorPercentage())
                .bachelorBacklogs(candidate.getBachelorBacklogs())

                // Master
                .masterDegree(candidate.getMasterDegree())
                .masterSpecialization(candidate.getMasterSpecialization())
                .masterCollegeName(candidate.getMasterCollegeName())
                .masterUniversityName(candidate.getMasterUniversityName())
                .masterRegistrationNumber(candidate.getMasterRegistrationNumber())
                .masterModeOfStudy(candidate.getMasterModeOfStudy())
                .masterStartYear(candidate.getMasterStartYear())
                .masterEndYear(candidate.getMasterEndYear())
                .masterPercentage(candidate.getMasterPercentage())

                .technicalSkills(
                        candidate.getTechnicalSkills() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(candidate.getTechnicalSkills())
                )

                .build();
    }

    @Override
    public CandidateProfileDto getCandidateProfile(Long userId) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Candidate candidate = candidateRepository.findById(userId)
                .orElse(new Candidate());

        return CandidateProfileDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .appliedRole(user.getAppliedRole())
                .candidateType(user.getCandidateType())
                .address(candidate.getAddress())
                .panNumber(candidate.getPanNumber())
                .aadhaarNumber(candidate.getAadhaarNumber())
                .build();
    }

    @Override
    public CandidateProfileDto saveCandidateProfile(Long userId,
                                                      CandidateProfileDto dto) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (candidateRepository.existsByPanNumber(dto.getPanNumber())) {
            throw new RuntimeException("PAN Number already exists.");
        }

        if (candidateRepository.existsByAadhaarNumber(dto.getAadhaarNumber())) {
            throw new RuntimeException("Aadhaar Number already exists.");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAppliedRole(dto.getAppliedRole());
        user.setCandidateType(dto.getCandidateType());

        candidateRepository.save(user);

        user.setAddress(dto.getAddress());
        user.setPanNumber(dto.getPanNumber());
        user.setAadhaarNumber(dto.getAadhaarNumber());

        candidateRepository.save(user);

        log.info("Candidate profile created for User ID : {}", userId);

        return dto;
    }

    @Override
    public CandidateProfileDto updateCandidateProfile(Long userId,
                                                        CandidateProfileDto dto) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Candidate candidate = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAppliedRole(dto.getAppliedRole());
        user.setCandidateType(dto.getCandidateType());

        candidateRepository.save(user);

        candidate.setAddress(dto.getAddress());
        candidate.setPanNumber(dto.getPanNumber());
        candidate.setAadhaarNumber(dto.getAadhaarNumber());

        candidateRepository.save(candidate);

        log.info("Candidate profile updated for User ID : {}", userId);

        return dto;
    }

    @Override
    public void saveCandidateProfile(Candidate candidate) {

        candidateRepository.save(candidate);

        log.info("Candidate profile created for User ID : {}", candidate.getId());
    }


    // ===========================
    // Education details
    // ===========================

    @Override
    public void saveEducation(CandidateEducationDto dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Candidate user = candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        // 10th
        candidate.setTenthSchoolName(dto.getTenthSchoolName());
        candidate.setTenthBoard(dto.getTenthBoard());
        candidate.setTenthSchoolLocation(dto.getTenthSchoolLocation());
        candidate.setTenthRegistrationNumber(dto.getTenthRegistrationNumber());
        candidate.setTenthPassingYear(dto.getTenthPassingYear());
        candidate.setTenthPercentage(dto.getTenthPercentage());
        candidate.setTenthMarksCard(convertFileToBytes(dto.getTenthMarksCard()));
        candidate.setTenthMarksCardType(getContentType(dto.getTenthMarksCard()));

        // PUC
        candidate.setPucInstitutionName(dto.getPucInstitutionName());
        candidate.setPucBoardUniversity(dto.getPucBoardUniversity());
        candidate.setPucStream(dto.getPucStream());
        candidate.setPucRegistrationNumber(dto.getPucRegistrationNumber());
        candidate.setPucPassingYear(dto.getPucPassingYear());
        candidate.setPucPercentage(dto.getPucPercentage());
        candidate.setPucMarksCard(convertFileToBytes(dto.getPucMarksCard()));
        candidate.setPucMarksCardType(getContentType(dto.getPucMarksCard()));

        // Bachelor
        candidate.setBachelorDegree(dto.getBachelorDegree());
        candidate.setBachelorSpecialization(dto.getBachelorSpecialization());
        candidate.setBachelorCollegeName(dto.getBachelorCollegeName());
        candidate.setBachelorUniversityName(dto.getBachelorUniversityName());
        candidate.setBachelorUsnNumber(dto.getBachelorUsnNumber());
        candidate.setBachelorStartYear(dto.getBachelorStartYear());
        candidate.setBachelorEndYear(dto.getBachelorEndYear());
        candidate.setBachelorPercentage(dto.getBachelorPercentage());
        candidate.setBachelorBacklogs(dto.getBachelorBacklogs());
        candidate.setBachelorMarksCard(convertFileToBytes(dto.getBachelorMarksCard()));
        candidate.setBachelorMarksCardType(getContentType(dto.getBachelorMarksCard()));
        candidate.setBachelorDegreeCertificate(convertFileToBytes(dto.getBachelorDegreeCertificate()));
        candidate.setBachelorDegreeCertificateType(getContentType(dto.getBachelorDegreeCertificate()));

        // Master
        candidate.setMasterDegree(dto.getMasterDegree());
        candidate.setMasterSpecialization(dto.getMasterSpecialization());
        candidate.setMasterCollegeName(dto.getMasterCollegeName());
        candidate.setMasterUniversityName(dto.getMasterUniversityName());
        candidate.setMasterRegistrationNumber(dto.getMasterRegistrationNumber());
        candidate.setMasterModeOfStudy(dto.getMasterModeOfStudy());
        candidate.setMasterStartYear(dto.getMasterStartYear());
        candidate.setMasterEndYear(dto.getMasterEndYear());
        candidate.setMasterPercentage(dto.getMasterPercentage());
        candidate.setMasterMarksCard(convertFileToBytes(dto.getMasterMarksCard()));
        candidate.setMasterMarksCardType(getContentType(dto.getMasterMarksCard()));
        candidate.setMasterConsolidatedMarksCard(convertFileToBytes(dto.getMasterConsolidatedMarksCard()));
        candidate.setMasterConsolidatedMarksCardType(getContentType(dto.getMasterConsolidatedMarksCard()));
        candidate.setMasterDegreeCertificate(convertFileToBytes(dto.getMasterDegreeCertificate()));
        candidate.setMasterDegreeCertificateType(getContentType(dto.getMasterDegreeCertificate()));

        candidate.setTechnicalSkills(dto.getTechnicalSkills());

        candidateRepository.save(candidate);

        log.info("Education details saved for candidate ID: {}", candidate.getId());
    }

    @Override
    public void updateEducation(CandidateEducationDto dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Candidate user = candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        // 10th
        candidate.setTenthSchoolName(dto.getTenthSchoolName());
        candidate.setTenthBoard(dto.getTenthBoard());
        candidate.setTenthSchoolLocation(dto.getTenthSchoolLocation());
        candidate.setTenthRegistrationNumber(dto.getTenthRegistrationNumber());
        candidate.setTenthPassingYear(dto.getTenthPassingYear());
        candidate.setTenthPercentage(dto.getTenthPercentage());
        if (dto.getTenthMarksCard() != null && !dto.getTenthMarksCard().isEmpty()) {
            candidate.setTenthMarksCard(convertFileToBytes(dto.getTenthMarksCard()));
            candidate.setTenthMarksCardType(getContentType(dto.getTenthMarksCard()));
        }

        // PUC
        candidate.setPucInstitutionName(dto.getPucInstitutionName());
        candidate.setPucBoardUniversity(dto.getPucBoardUniversity());
        candidate.setPucStream(dto.getPucStream());
        candidate.setPucRegistrationNumber(dto.getPucRegistrationNumber());
        candidate.setPucPassingYear(dto.getPucPassingYear());
        candidate.setPucPercentage(dto.getPucPercentage());
        if (dto.getPucMarksCard() != null && !dto.getPucMarksCard().isEmpty()) {
            candidate.setPucMarksCard(convertFileToBytes(dto.getPucMarksCard()));
            candidate.setPucMarksCardType(getContentType(dto.getPucMarksCard()));
        }

        // Bachelor
        candidate.setBachelorDegree(dto.getBachelorDegree());
        candidate.setBachelorSpecialization(dto.getBachelorSpecialization());
        candidate.setBachelorCollegeName(dto.getBachelorCollegeName());
        candidate.setBachelorUniversityName(dto.getBachelorUniversityName());
        candidate.setBachelorUsnNumber(dto.getBachelorUsnNumber());
        candidate.setBachelorStartYear(dto.getBachelorStartYear());
        candidate.setBachelorEndYear(dto.getBachelorEndYear());
        candidate.setBachelorPercentage(dto.getBachelorPercentage());
        candidate.setBachelorBacklogs(dto.getBachelorBacklogs());
        if (dto.getBachelorMarksCard() != null && !dto.getBachelorMarksCard().isEmpty()) {
            candidate.setBachelorMarksCard(convertFileToBytes(dto.getBachelorMarksCard()));
            candidate.setBachelorMarksCardType(getContentType(dto.getBachelorMarksCard()));
        }
        if (dto.getBachelorDegreeCertificate() != null && !dto.getBachelorDegreeCertificate().isEmpty()) {
            candidate.setBachelorDegreeCertificate(convertFileToBytes(dto.getBachelorDegreeCertificate()));
            candidate.setBachelorDegreeCertificateType(getContentType(dto.getBachelorDegreeCertificate()));
        }

        // Master
        candidate.setMasterDegree(dto.getMasterDegree());
        candidate.setMasterSpecialization(dto.getMasterSpecialization());
        candidate.setMasterCollegeName(dto.getMasterCollegeName());
        candidate.setMasterUniversityName(dto.getMasterUniversityName());
        candidate.setMasterRegistrationNumber(dto.getMasterRegistrationNumber());
        candidate.setMasterModeOfStudy(dto.getMasterModeOfStudy());
        candidate.setMasterStartYear(dto.getMasterStartYear());
        candidate.setMasterEndYear(dto.getMasterEndYear());
        candidate.setMasterPercentage(dto.getMasterPercentage());
        if (dto.getMasterMarksCard() != null && !dto.getMasterMarksCard().isEmpty()) {
            candidate.setMasterMarksCard(convertFileToBytes(dto.getMasterMarksCard()));
            candidate.setMasterMarksCardType(getContentType(dto.getMasterMarksCard()));
        }
        if (dto.getMasterConsolidatedMarksCard() != null && !dto.getMasterConsolidatedMarksCard().isEmpty()) {
            candidate.setMasterConsolidatedMarksCard(convertFileToBytes(dto.getMasterConsolidatedMarksCard()));
            candidate.setMasterConsolidatedMarksCardType(getContentType(dto.getMasterConsolidatedMarksCard()));
        }
        if (dto.getMasterDegreeCertificate() != null && !dto.getMasterDegreeCertificate().isEmpty()) {
            candidate.setMasterDegreeCertificate(convertFileToBytes(dto.getMasterDegreeCertificate()));
            candidate.setMasterDegreeCertificateType(getContentType(dto.getMasterDegreeCertificate()));
        }

        candidate.setTechnicalSkills(dto.getTechnicalSkills());

        candidateRepository.save(candidate);

        log.info("Education details updated for candidate ID: {}", candidate.getId());
    }

    @Override
    public CandidateEducationDto getEducationByCandidateId(Long candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        return mapToEducationDto(candidate);
    }

    @Override
    public CandidateEducationDto getEducationByEmail(String email) {

        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        return mapToEducationDto(candidate);
    }

    @Override
    public List<CandidateEducationDto> searchEducation(String keyword) {

        return candidateRepository
                .searchEducation(keyword)
                .stream()
                .map(this::mapToEducationDto)
                .toList();
    }

    @Override
    public void deleteEducation(Long candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        candidate.setTenthSchoolName(null);
        candidate.setTenthBoard(null);
        candidate.setTenthSchoolLocation(null);
        candidate.setTenthRegistrationNumber(null);
        candidate.setTenthPassingYear(null);
        candidate.setTenthPercentage(null);
        candidate.setTenthMarksCard(null);
        candidate.setTenthMarksCardType(null);

        candidate.setPucInstitutionName(null);
        candidate.setPucBoardUniversity(null);
        candidate.setPucStream(null);
        candidate.setPucRegistrationNumber(null);
        candidate.setPucPassingYear(null);
        candidate.setPucPercentage(null);
        candidate.setPucMarksCard(null);
        candidate.setPucMarksCardType(null);

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

        candidate.setTechnicalSkills(new ArrayList<>());

        candidateRepository.save(candidate);

        log.info("Education details deleted for candidate ID: {}", candidateId);
    }
}