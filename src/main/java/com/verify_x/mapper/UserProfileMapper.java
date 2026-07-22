//package com.verify_x.mapper;
//
//import com.verify_x.dto.UserProfileResponseDto;
//import com.verify_x.entity.UserProfile;
//import org.springframework.stereotype.Component;
//
//@Component
//public class UserProfileMapper {
//
//    public UserProfileResponseDto toResponse(C userProfile) {
//
//        return UserProfileResponseDto.builder()
//                .userId(userProfile.getUserId())
//                .firstName(userProfile.getFirstName())
//                .lastName(userProfile.getLastName())
//                .email(userProfile.getEmail())
//                .phone(userProfile.getPhone())
//                .collegeName(userProfile.getCollegeName())
//                .higherEducation(userProfile.getHigherEducation())
//                .graduationYear(userProfile.getGraduationYear())
//                .currentAddress(userProfile.getCurrentAddress())
//                .permanentAddress(userProfile.getPermanentAddress())
//                .currentCompany(userProfile.getCurrentCompany())
//                .employmentStatus(userProfile.getEmploymentStatus())
//                .experience(userProfile.getExperience())
//                .designation(userProfile.getDesignation())
//                .ctc(userProfile.getCtc())
//                .createdAt(userProfile.getCreatedAt())
//                .build();
//    }
//}