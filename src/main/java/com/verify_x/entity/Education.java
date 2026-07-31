package com.verify_x.entity;

import com.verify_x.enums.BacklogStatus;
import com.verify_x.enums.BoardType;
import com.verify_x.enums.ModeOfStudy;
import com.verify_x.enums.StreamType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "education_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    // =====================================================
    // 10th Details
    // =====================================================

    private String tenthSchoolName;

    @Enumerated(EnumType.STRING)
    private BoardType tenthBoard;

    private String tenthSchoolLocation;

    private String tenthRollNumber;

    private Integer tenthPassingYear;

    private Double tenthPercentage;


    private String tenthMarksCardName;

    private String tenthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] tenthMarksCard;


    // =====================================================
    // 12th Details
    // =====================================================

    private String twelfthInstitutionName;

    private String twelfthBoardUniversity;

    @Enumerated(EnumType.STRING)
    private StreamType twelfthStream;

    private String twelfthRegistrationNumber;

    private Integer twelfthPassingYear;

    private Double twelfthPercentage;


    private String twelfthMarksCardName;

    private String twelfthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] twelfthMarksCard;
    // =====================================================
    // Bachelor's
    // =====================================================

    private String degreeName;

    private String specialization;

    private String collegeName;

    private String universityName;

    private String usnNumber;

    private Integer degreeStartYear;

    private Integer degreeEndYear;

    private Double degreePercentage;

    @Enumerated(EnumType.STRING)
    private BacklogStatus backlogStatus;


    private String degreeCertificateName;

    private String degreeCertificateContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] degreeCertificate;
    // =====================================================
    // Master's
    // =====================================================

    private String mastersDegree;

    private String mastersSpecialization;

    private String mastersCollege;

    private String mastersUniversity;

    private String mastersRegistrationNumber;

    @Enumerated(EnumType.STRING)
    private ModeOfStudy modeOfStudy;

    private Integer mastersStartYear;

    private Integer mastersEndYear;

    private Double mastersPercentage;


    private String mastersMarksCardName;

    private String mastersMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] mastersMarksCard;

    private String mastersDegreeCertificateName;

    private String mastersDegreeCertificateContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] mastersDegreeCertificate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}