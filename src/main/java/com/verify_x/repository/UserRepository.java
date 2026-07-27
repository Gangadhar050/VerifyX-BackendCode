//package com.verify_x.repository;
//
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByEmail(String email);
//
//    Optional<User> findByPhoneNumber(String phoneNumber);
//
//    boolean existsByEmail(String email);
//
//    boolean existsByPhoneNumber(String phoneNumber);
//    boolean existsByUsername(String username);
//    void deleteByEmail(String email);
//}