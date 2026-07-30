package com.verify_x.dto;

import com.verify_x.enums.TechnicalSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEducationDto {

    // ===========================
    // 10th Details
    // ===========================

    private String tenthSchoolName;
    private String tenthBoard;
    private String tenthSchoolLocation;
    private String tenthRegistrationNumber;
    private Integer tenthPassingYear;
    private Double tenthPercentage;

    private MultipartFile tenthMarksCard;

    // ===========================
    // PUC / 12th / Diploma
    // ===========================

    private String pucInstitutionName;
    private String pucBoardUniversity;
    private String pucStream;
    private String pucRegistrationNumber;
    private Integer pucPassingYear;
    private Double pucPercentage;

    private MultipartFile pucMarksCard;

    // ===========================
    // Bachelor's Degree
    // ===========================

    private String bachelorDegree;
    private String bachelorSpecialization;
    private String bachelorCollegeName;
    private String bachelorUniversityName;
    private String bachelorUsnNumber;
    private Integer bachelorStartYear;
    private Integer bachelorEndYear;
    private Double bachelorPercentage;
    private String bachelorBacklogs;

    private MultipartFile bachelorMarksCard;

    private MultipartFile bachelorDegreeCertificate;

    // ===========================
    // Master's Degree
    // ===========================

    private String masterDegree;
    private String masterSpecialization;
    private String masterCollegeName;
    private String masterUniversityName;
    private String masterRegistrationNumber;
    private String masterModeOfStudy;
    private Integer masterStartYear;
    private Integer masterEndYear;
    private Double masterPercentage;

    private MultipartFile masterMarksCard;

    private MultipartFile masterConsolidatedMarksCard;

    private MultipartFile masterDegreeCertificate;

    // ===========================
    // Technical Skills
    // ===========================

    private List<TechnicalSkill> technicalSkills;
}