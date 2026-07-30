package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Employment;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EmploymentRepository;
import com.verify_x.services.CandidateManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateManagementServiceImpl implements CandidateManagementService {

    private final CandidateRepository candidateRepository;
    private final EmploymentRepository employmentRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;

    private CandidateSummaryDto mapToSummary(Candidate candidate) {
        Employment employment = employmentRepository.findByCandidate(candidate).orElse(null);

        return CandidateSummaryDto.builder()
                .id(candidate.getId())
                .fullName(candidate.getUsername())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .candidateType(candidate.getCandidateType())
                .skills(candidate.getTechnicalSkills())
                .uanNumber(employment != null ? employment.getUanNumber() : null)
                .uanVerified(employment != null && Boolean.TRUE.equals(employment.getUanVerified()))
                .applicationStatus(candidate.getApplicationStatus())
                .build();
    }

    private CandidateDocumentDto mapDocumentToDto(CandidateDocument document) {
        return CandidateDocumentDto.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public PagedResponse<CandidateSummaryDto> getAllCandidates(
            String keyword,
            CandidateType candidateType,
            ApplicationStatus applicationStatus,
            int page,
            int size
    ) {

        List<CandidateSummaryDto> candidates = candidateRepository.findAll()
                .stream()
                .map(this::mapToSummary)
                .toList();

        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();

            candidates = candidates.stream()
                    .filter(c ->
                            (c.getFullName() != null && c.getFullName().toLowerCase().contains(k))
                                    || (c.getEmail() != null && c.getEmail().toLowerCase().contains(k))
                                    || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(k))
                    )
                    .toList();
        }

        if (candidateType != null) {
            candidates = candidates.stream()
                    .filter(c -> c.getCandidateType() == candidateType)
                    .toList();
        }

        if (applicationStatus != null) {
            candidates = candidates.stream()
                    .filter(c -> c.getApplicationStatus() == applicationStatus)
                    .toList();
        }

        int total = candidates.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);

        return PagedResponse.<CandidateSummaryDto>builder()
                .content(candidates.subList(from, to))
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    @Override
    public CandidateDetailsDto getCandidateDetails(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        Employment employment = employmentRepository.findByCandidate(candidate).orElse(null);

        List<CandidateDocumentDto> documents = candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapDocumentToDto)
                .toList();

        CandidateProfileDto profile = CandidateProfileDto.builder()
                .username(candidate.getUsername())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .address(candidate.getAddress())
                .panNumber(candidate.getPanNumber())
                .aadhaarNumber(candidate.getAadhaarNumber())
                .appliedRole(candidate.getAppliedRole())
                .candidateType(candidate.getCandidateType())
                .build();

        // NOTE: CandidateEducationDto's file fields are MultipartFile and cannot
        // be reconstructed from stored byte[] data, so they are intentionally
        // omitted here (left null). Use a dedicated download endpoint if you
        // need to serve the actual file bytes.
        CandidateEducationDto education = CandidateEducationDto.builder()

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

                // Technical Skills
                .technicalSkills(candidate.getTechnicalSkills())

                .build();

        EmploymentDetailsDto employmentDto = employment == null ? null : EmploymentDetailsDto.builder()
                .previousCompanyName(employment.getPreviousCompanyName())
                .previousDesignation(employment.getPreviousDesignation())
                .totalExperience(employment.getTotalExperience())
                .lastCTC(employment.getLastCTC())
                .lastWorkingDay(employment.getLastWorkingDay())
                .uanNumber(employment.getUanNumber())
                .employmentStatus(employment.getEmploymentStatus())
                .currentCompany(employment.getCurrentCompany())
                .currentDesignation(employment.getCurrentDesignation())
                .currentCTC(employment.getCurrentCTC())
                .noticePeriod(employment.getNoticePeriod())
                .offerLetterStatus(employment.getOfferLetterStatus())
                .offerCompanyName(employment.getOfferCompanyName())
                .offeredCTC(employment.getOfferedCTC())
                .joiningDate(employment.getJoiningDate())
                .offerReferenceNumber(employment.getOfferReferenceNumber())
                .build();

        return CandidateDetailsDto.builder()
                .profile(profile)
                .education(education)
                .employment(employmentDto)
                .documents(documents)
                .applicationStatus(candidate.getApplicationStatus())
                .remarks(candidate.getRemarks())
                .uanVerified(employment != null && Boolean.TRUE.equals(employment.getUanVerified()))
                .uanVerifiedBy(employment != null ? employment.getUanVerifiedBy() : null)
                .build();
    }

    @Override
    public CandidateSummaryDto createCandidate(UserRegistrationDto dto) {

        if (candidateRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        if (candidateRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists.");
        }

        Candidate candidate = Candidate.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(dto.getPassword())
                .appliedRole(dto.getAppliedRole())
                .candidateType(dto.getCandidateType())
                .applicationStatus(ApplicationStatus.PENDING_VERIFICATION)
                .build();

        candidateRepository.save(candidate);

        return mapToSummary(candidate);
    }

    @Override
    public void deleteCandidate(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        candidateDocumentRepository.findByCandidate(candidate)
                .forEach(candidateDocumentRepository::delete);

        employmentRepository.findByCandidate(candidate)
                .ifPresent(employmentRepository::delete);

        candidateRepository.delete(candidate);
        log.info("Candidate {} deleted by HR.", candidateId);
    }

    @Override
    public void verifyUan(Long candidateId, String verifiedBy) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        Employment employment = employmentRepository.findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Employment details not found."));

        if (employment.getUanNumber() == null || !employment.getUanNumber().matches("^\\d{12}$")) {
            throw new BadRequestException("UAN number must contain exactly 12 digits before verification.");
        }

        employment.setUanVerified(true);
        employment.setUanVerifiedBy(verifiedBy);
        employment.setUanVerifiedAt(LocalDateTime.now());
        employmentRepository.save(employment);

        log.info("UAN verified for candidate {} by {}", candidateId, verifiedBy);
    }

    @Override
    public void updateApplicationStatus(Long candidateId, ApplicationStatusUpdateDto dto, String reviewedBy) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        if (dto.getStatus() == ApplicationStatus.APPROVED) {
            Employment employment = employmentRepository.findByCandidate(candidate).orElse(null);

            boolean uanOk = candidate.getCandidateType() != CandidateType.EXPERIENCED
                    || (employment != null && Boolean.TRUE.equals(employment.getUanVerified()));

            boolean hasRejectedDocs = candidateDocumentRepository.findByCandidate(candidate)
                    .stream()
                    .anyMatch(doc -> doc.getStatus() == DocumentStatus.REJECTED);

            if (!uanOk) {
                throw new BadRequestException("Cannot approve. UAN must be verified for experienced candidates.");
            }
            if (hasRejectedDocs) {
                throw new BadRequestException("Cannot approve. Candidate has rejected documents pending re-upload.");
            }
        }

        candidate.setApplicationStatus(dto.getStatus());
        candidate.setRemarks(dto.getRemarks());
        candidateRepository.save(candidate);

        log.info("Application status for candidate {} set to {} by {}", candidateId, dto.getStatus(), reviewedBy);
    }
}