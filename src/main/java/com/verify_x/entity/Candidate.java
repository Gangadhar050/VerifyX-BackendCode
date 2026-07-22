package com.verify_x.entity;

import com.verify_x.enums.Language;
import com.verify_x.enums.SoftSkill;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String name;

    private String email;

    private String phoneNumber;

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


    // Education

    private String highestEducation;

    private String college;

    private Integer passingYear;

    private Double percentage;


// Technical Skills

    @ElementCollection(fetch = FetchType.EAGER, targetClass = TechnicalSkill.class)
    @CollectionTable(
            name = "candidate_technical_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    private List<TechnicalSkill> technicalSkills;


// Soft Skills

    @ElementCollection(fetch = FetchType.EAGER, targetClass = SoftSkill.class)
    @CollectionTable(
            name = "candidate_soft_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    private List<SoftSkill> softSkills;


// Languages

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Language.class)
    @CollectionTable(
            name = "candidate_languages",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    private List<Language> languages;


//Emplyment details
    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Employment employment;
}

