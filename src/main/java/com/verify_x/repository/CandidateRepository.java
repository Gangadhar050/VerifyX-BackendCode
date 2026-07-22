package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByUserId(Long userId);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    //Education
    Optional<Candidate> findByUser(User user);

    Optional<Candidate> findByUserEmail(String email);

    List<Candidate> findByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
            String username,
            String email
    );

}