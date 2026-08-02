//package com.verify_x.serviceImpl;
//
//
//
//import java.time.LocalDate;
//
//public class DocumentParseResult {
//
//    private final boolean resignationKeywordFound;
//    private final boolean acceptanceKeywordFound;
//    private final boolean relievingKeywordFound;
//    private final LocalDate extractedLastWorkingDay;
//
//    public DocumentParseResult(boolean resignationKeywordFound,
//                               boolean acceptanceKeywordFound,
//                               boolean relievingKeywordFound,
//                               LocalDate extractedLastWorkingDay) {
//        this.resignationKeywordFound = resignationKeywordFound;
//        this.acceptanceKeywordFound  = acceptanceKeywordFound;
//        this.relievingKeywordFound   = relievingKeywordFound;
//        this.extractedLastWorkingDay = extractedLastWorkingDay;
//    }
//
//    public boolean isResignationKeywordFound()    { return resignationKeywordFound; }
//    public boolean isAcceptanceKeywordFound()     { return acceptanceKeywordFound; }
//    public boolean isRelievingKeywordFound()      { return relievingKeywordFound; }
//    public LocalDate getExtractedLastWorkingDay() { return extractedLastWorkingDay; }
//}
