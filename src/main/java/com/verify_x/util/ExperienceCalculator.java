//package com.verify_x.util;
//
//import java.time.LocalDate;
//import java.time.Period;
//import java.time.temporal.ChronoUnit;
//import java.util.Comparator;
//import java.util.List;
//
//import com.verify_x.entity.EmploymentHistory;
//
//public class ExperienceCalculator {
//    public static String calculateDuration(String joiningDate, String relievingDate) {
//
//        if (joiningDate == null || joiningDate.isBlank()) {
//            return "";
//        }
//
//        LocalDate start = LocalDate.parse(joiningDate);
//
//        LocalDate end;
//
//        if (relievingDate == null
//                || relievingDate.isBlank()
//                || relievingDate.equalsIgnoreCase("Present")) {
//
//            end = LocalDate.now();
//
//        } else {
//
//            end = LocalDate.parse(relievingDate);
//        }
//
//        Period period = Period.between(start, end);
//
//        return period.getYears() + " Years " +
//                period.getMonths() + " Months";
//    }
//
//    public static double calculateTotalExperience(List<EmploymentHistory> histories) {
//
//        long totalMonths = 0;
//
//        for (EmploymentHistory history : histories) {
//
//            if (history.getJoiningDate() == null)
//                continue;
//
//            LocalDate start = LocalDate.parse(history.getJoiningDate());
//
//            LocalDate end;
//
//            if (history.getRelievingDate() == null
//                    || history.getRelievingDate().isBlank()
//                    || history.getRelievingDate().equalsIgnoreCase("Present")) {
//
//                end = LocalDate.now();
//
//            } else {
//
//                end = LocalDate.parse(history.getRelievingDate());
//            }
//
//            totalMonths += ChronoUnit.MONTHS.between(start, end);
//        }
//
//        return totalMonths / 12.0;
//    }
//
//    public static String calculateGap(List<EmploymentHistory> histories) {
//
//        boolean currentlyWorking = histories.stream()
//                .anyMatch(h ->
//                        "Present".equalsIgnoreCase(h.getRelievingDate()));
//
//        if (currentlyWorking) {
//
//            return "Currently Working";
//        }
//
//        LocalDate latestRelievingDate = histories.stream()
//
//                .map(EmploymentHistory::getRelievingDate)
//
//                .filter(date -> date != null && !date.isBlank())
//
//                .map(LocalDate::parse)
//
//                .max(Comparator.naturalOrder())
//
//                .orElse(null);
//
//        if (latestRelievingDate == null) {
//
//            return "";
//        }
//
//        Period period = Period.between(latestRelievingDate, LocalDate.now());
//
//        return period.getYears() + " Years "
//                + period.getMonths() + " Months";
//    }
//}
//
////public void setCalulation() {
////
////    for (EmploymentHistory history : histories) {
////
////        history.setDuration(
////
////                ExperienceCalculator.calculateDuration(
////
////                        history.getJoiningDate(),
////
////                        history.getRelievingDate()
////                )
////        );
////    }
////
////    resume.setTotalExperience(
////
////            ExperienceCalculator.calculateTotalExperience(histories)
////    );
////
////    String gap = ExperienceCalculator.calculateGap(histories);
////
////    screening.setEmploymentGap(gap);
////}