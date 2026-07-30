package com.verify_x.entity;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.Role;
import com.verify_x.enums.TechnicalSkill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String appliedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateType candidateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 500)
    private String address;

    @Column(unique = true, length = 10)
    private String panNumber;

    @Column(unique = true, length = 12)
    private String aadhaarNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ===========================
    // Education Details
    // ===========================

    // ---------- 10th ----------
    private String tenthSchoolName;
    private String tenthBoard;
    private String tenthSchoolLocation;
    private String tenthRegistrationNumber;
    private Integer tenthPassingYear;
    private Double tenthPercentage;

    private String tenthMarksCardType;

    @Lob
    @Column(name = "tenth_marks_card", columnDefinition = "LONGBLOB")
    private byte[] tenthMarksCard;

    // ---------- PUC / 12th / Diploma ----------
    private String pucInstitutionName;
    private String pucBoardUniversity;
    private String pucStream;
    private String pucRegistrationNumber;
    private Integer pucPassingYear;
    private Double pucPercentage;

    private String pucMarksCardType;

    @Lob
    @Column(name = "puc_marks_card", columnDefinition = "LONGBLOB")
    private byte[] pucMarksCard;

    // ---------- Bachelor's Degree ----------
    private String bachelorDegree;
    private String bachelorSpecialization;
    private String bachelorCollegeName;
    private String bachelorUniversityName;
    private String bachelorUsnNumber;
    private Integer bachelorStartYear;
    private Integer bachelorEndYear;
    private Double bachelorPercentage;
    private String bachelorBacklogs;

    private String bachelorMarksCardType;

    @Lob
    @Column(name = "bachelor_marks_card", columnDefinition = "LONGBLOB")
    private byte[] bachelorMarksCard;

    private String bachelorDegreeCertificateType;

    @Lob
    @Column(name = "bachelor_degree_certificate", columnDefinition = "LONGBLOB")
    private byte[] bachelorDegreeCertificate;

    // ---------- Master's Degree ----------
    private String masterDegree;
    private String masterSpecialization;
    private String masterCollegeName;
    private String masterUniversityName;
    private String masterRegistrationNumber;
    private String masterModeOfStudy;
    private Integer masterStartYear;
    private Integer masterEndYear;
    private Double masterPercentage;

    private String masterMarksCardType;

    @Lob
    @Column(name = "master_marks_card", columnDefinition = "LONGBLOB")
    private byte[] masterMarksCard;

    private String masterConsolidatedMarksCardType;

    @Lob
    @Column(name = "master_consolidated_marks_card", columnDefinition = "LONGBLOB")
    private byte[] masterConsolidatedMarksCard;

    private String masterDegreeCertificateType;

    @Lob
    @Column(name = "master_degree_certificate", columnDefinition = "LONGBLOB")
    private byte[] masterDegreeCertificate;

    // ===========================
    // Technical Skills
    // ===========================
    @ElementCollection(fetch = FetchType.EAGER, targetClass = TechnicalSkill.class)
    @CollectionTable(
            name = "candidate_technical_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    private List<TechnicalSkill> technicalSkills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING_VERIFICATION;

    @Column(length = 1000)
    private String remarks;

    // ===========================
    // Employment details
    // ===========================
    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Employment employment;

    @OneToMany(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<CandidateDocument> documents;
}