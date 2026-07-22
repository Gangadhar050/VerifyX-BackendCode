//package com.verify_x.mapper;
//
//import com.verify_x.dto.ScreeningRequestExperienceDto;
//import com.verify_x.dto.ScreeningRequestFresherDto;
//import com.verify_x.dto.ScreeningResponseExperienceDto;
//import com.verify_x.dto.ScreeningResponseFresherDto;
//import com.verify_x.entity.EmploymentHistory;
//import com.verify_x.entity.Screening;
//import com.verify_x.enums.EmploymentStatus;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ScreeningMapper {
//
//    public Screening toEntity(ScreeningRequestExperienceDto request) {
//
//        if (request == null) {
//            return null;
//        }
//
//        Screening screening = Screening.builder()
//                .employmentStatus(request.getEmploymentStatus())
//                .hasEpf(request.getHasEpf())
//                .uan(request.getUan())
//                .graduationMonthYear(null)
//                .build();
//
//        applyEmploymentDetails(buildEmploymentHistory(request), screening);
//
//        return screening;
//    }
//
//    public Screening toEntity(ScreeningRequestFresherDto request) {
//
//        if (request == null) {
//            return null;
//        }
//
//        return Screening.builder()
//                .employmentStatus(EmploymentStatus.FRESHER)
//                .hasEpf(false)
//                .uan(null)
//                .graduationMonthYear(request.getGraduationMonthYear())
//                .build();
//    }
//
//    public ScreeningResponseExperienceDto toResponse(Screening screening) {
//
//        if (screening == null) {
//            return null;
//        }
//
//        return ScreeningResponseExperienceDto.builder()
//                .id(screening.getId())
//                .employmentStatus(screening.getEmploymentStatus())
//                .hasEpf(screening.getHasEpf())
//                .uan(screening.getUan())
//                .graduationMonthYear(screening.getGraduationMonthYear())
//                .totalGapMonths(screening.getTotalGapMonths())
//                //.gapReason(screening.getGapReason())
//                .screeningStatus(screening.getScreeningStatus())
//                .companyVerificationStatus(screening.getCompanyVerificationStatus())
//                .documentVerificationStatus(screening.getDocumentVerificationStatus())
//                .remarks(screening.getRemarks())
//                .build();
//    }
//
//    public ScreeningResponseFresherDto toResponseFresher(Screening screening) {
//
//        if (screening == null) {
//            return null;
//        }
//
//        return ScreeningResponseFresherDto.builder()
//                .id(screening.getId())
//                .employmentStatus(screening.getEmploymentStatus())
//                .graduationMonthYear(screening.getGraduationMonthYear())
//                .totalGapMonths(screening.getTotalGapMonths())
//                //.gapReason(screening.getGapReason())
//                .screeningStatus(screening.getScreeningStatus())
//                .documentVerificationStatus(screening.getDocumentVerificationStatus())
//                .remarks(screening.getRemarks())
//                .build();
//    }
//
//    public void updateEntity(
//            Screening screening,
//            ScreeningRequestExperienceDto request) {
//
//        screening.setEmploymentStatus(request.getEmploymentStatus());
//        screening.setHasEpf(request.getHasEpf());
//        screening.setUan(request.getUan());
//        screening.setGraduationMonthYear(null);
////        screening.setGapReason(request.getGapReason());
//
//
//        applyEmploymentDetails(buildEmploymentHistory(request), screening);
//    }
//
//    public void updateEntity(
//            Screening screening,
//            ScreeningRequestFresherDto request) {
//
//        if (request != null) {
//            screening.setEmploymentStatus(EmploymentStatus.FRESHER);
//            screening.setHasEpf(false);
//            screening.setUan(null);
//            screening.setGraduationMonthYear(request.getGraduationMonthYear());
//        }
//    }
//
//    private EmploymentHistory buildEmploymentHistory(ScreeningRequestExperienceDto request) {
//
//        return EmploymentHistory.builder()
//                .companyName(request.getCompanyName())
//                .designation(request.getDesignation())
//                .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate().toString() : null)
//                .relievingDate(request.getRelievingDate() != null ? request.getRelievingDate().toString() : null)
//                .currentlyWorking(request.getCurrentlyWorking())
//                .build();
//    }
//
//    private void applyEmploymentDetails(EmploymentHistory details, Screening screening) {
//
//        if (screening.getEmploymentHistories() != null && !screening.getEmploymentHistories().isEmpty()) {
//
//            EmploymentHistory existing = screening.getEmploymentHistories().get(0);
//            existing.setCompanyName(details.getCompanyName());
//            existing.setDesignation(details.getDesignation());
//            existing.setJoiningDate(details.getJoiningDate());
//            existing.setRelievingDate(details.getRelievingDate());
//            existing.setCurrentlyWorking(details.getCurrentlyWorking());
//        } else {
//            screening.addEmploymentHistory(details);
//        }
//    }
//}