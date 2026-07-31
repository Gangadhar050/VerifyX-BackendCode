//package com.verify_x.util;
//
//import java.time.LocalDate;
//import java.time.Period;
//
//public final class EmploymentUtil {
//
//    private EmploymentUtil() {
//    }
//
//    public static int calculateDuration(
//            LocalDate joiningDate,
//            LocalDate relievingDate){
//
//        Period period = Period.between(joiningDate, relievingDate);
//
//        return period.getYears() * 12 + period.getMonths();
//    }
//
//}