package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.enums.TechnicalSkill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CandidateEducationRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    Optional<Candidate> findByPhoneNumber(String phoneNumber);

    Optional<Candidate> findByPanNumber(String panNumber);

    Optional<Candidate> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);


}