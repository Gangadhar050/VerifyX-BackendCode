//package com.verify_x.mapper;
//
//import com.verify_x.dto.EmploymentHistoryRequestDto;
//import com.verify_x.dto.EmploymentHistoryResponseDto;
//import com.verify_x.entity.EmploymentHistory;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EmploymentHistoryMapper {
//
//    public EmploymentHistoryResponseDto toResponse(EmploymentHistory entity) {
//
//        if (entity == null) {
//            return null;
//        }
//
//        return EmploymentHistoryResponseDto.builder()
//                .id(entity.getId())
//                .companyName(entity.getCompanyName())
//                .designation(entity.getDesignation())
//                .workLocation(entity.getWorkLocation())
//                .joiningDate(entity.getJoiningDate())
//                .relievingDate(entity.getRelievingDate())
//                .currentlyWorking(entity.getCurrentlyWorking())
//                .duration(entity.getDuration())
//                .employmentType(entity.getEmploymentType())
//                .description(entity.getDescription())
//                .companyWebsite(entity.getCompanyWebsite())
//                .HrEmail(entity.getHrEmail())
//                .HrMobile(entity.getHrMobile())
//                .durationInMonths(entity.getDuration())
//                .verificationStatus(entity.getVerificationStatus())
//                .remarks(entity.getRemarks())
//                .build();
//    }
//
//}