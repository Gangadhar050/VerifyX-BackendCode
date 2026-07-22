package com.verify_x.controller;


import com.verify_x.dto.CandidateEducationDto;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateService candidateService;

    /**
     * Get Candidate Profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> getProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        CandidateProfileDto profile =
                candidateService.getCandidateProfile(principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<CandidateProfileDto>builder()
                        .success(true)
                        .message("Candidate profile fetched successfully.")
                        .data(profile)
                        .build()
        );
    }

    /**
     * Save Candidate Profile
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> saveProfile(
            @Valid @RequestBody CandidateProfileDto dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        CandidateProfileDto profile =
                candidateService.saveCandidateProfile(
                        principal.getUserId(),
                        dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<CandidateProfileDto>builder()
                                .success(true)
                                .message("Candidate profile created successfully.")
                                .data(profile)
                                .build()
                );
    }

    /**
     * Update Candidate Profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> updateProfile(
            @Valid @RequestBody CandidateProfileDto dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        CandidateProfileDto profile =
                candidateService.updateCandidateProfile(
                        principal.getUserId(),
                        dto);

        return ResponseEntity.ok(
                ApiResponse.<CandidateProfileDto>builder()
                        .success(true)
                        .message("Candidate profile updated successfully.")
                        .data(profile)
                        .build()
        );
    }

    //EducationDetails

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<String>> saveEducation(
            @RequestBody CandidateEducationDto dto) {

        candidateService.saveEducation(dto);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Education Details Saved Successfully")
                        .data("Success")
                        .build()
        );
    }

    @PutMapping("/education")
    public ResponseEntity<ApiResponse<String>> updateEducation(
            @RequestBody CandidateEducationDto dto) {

        candidateService.updateEducation(dto);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Education details updated successfully.")
                        .data("Success")
                        .build()
        );
    }

    @GetMapping("/education/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateEducationDto>> getEducationByCandidateId(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                ApiResponse.<CandidateEducationDto>builder()
                        .success(true)
                        .message("Education details fetched successfully")
                        .data(candidateService.getEducationByCandidateId(candidateId))
                        .build()
        );
    }
    @GetMapping("/education/email")
    public ResponseEntity<ApiResponse<CandidateEducationDto>> getEducationByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                ApiResponse.<CandidateEducationDto>builder()
                        .success(true)
                        .message("Education details fetched successfully")
                        .data(candidateService.getEducationByEmail(email))
                        .build()
        );
    }
    @GetMapping("/education/search")
    public ResponseEntity<ApiResponse<List<CandidateEducationDto>>> searchEducation(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.<List<CandidateEducationDto>>builder()
                        .success(true)
                        .message("Education details fetched successfully")
                        .data(candidateService.searchEducation(keyword))
                        .build()
        );
    }
    @DeleteMapping("/education/{candidateId}")
    public ResponseEntity<ApiResponse<String>> deleteEducation(
            @PathVariable Long candidateId) {

        candidateService.deleteEducation(candidateId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Education deleted successfully")
                        .data("Deleted")
                        .build()
        );
    }
}

//package com.verify_x.controller;
//
//import com.verify_x.dto.CandidateEducationDto;
//import com.verify_x.dto.CandidateProfileDto;
//import com.verify_x.payload.ApiResponse;
//import com.verify_x.services.CandidateService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//        import java.util.List;
//
//@RestController
//@RequestMapping("/api/candidate")
//@RequiredArgsConstructor
//public class CandidateProfileController {
//
//    private final CandidateService candidateService;
//
//    // ===========================
//    // Candidate Profile
//    // ===========================
//
//    @GetMapping("/{candidateId}")
//    public ResponseEntity<ApiResponse<CandidateProfileDto>> getCandidateProfile(
//            @PathVariable Long candidateId) {
//
//        CandidateProfileDto profile =
//                candidateService.getCandidateProfile(candidateId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(true,
//                        "Candidate profile fetched successfully",
//                        profile)
//        );
//    }
//
//    @PostMapping("/{candidateId}")
//    public ResponseEntity<ApiResponse<CandidateProfileDto>> saveCandidateProfile(
//            @PathVariable Long candidateId,
//            @RequestBody CandidateProfileDto dto) {
//
//        CandidateProfileDto profile =
//                candidateService.saveCandidateProfile(candidateId, dto);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new ApiResponse<>(
//                        true,
//                        "Candidate profile created successfully",
//                        profile
//                ));
//    }
//
//    @PutMapping("/{candidateId}")
//    public ResponseEntity<ApiResponse<CandidateProfileDto>> updateCandidateProfile(
//            @PathVariable Long candidateId,
//            @RequestBody CandidateProfileDto dto) {
//
//        CandidateProfileDto profile =
//                candidateService.updateCandidateProfile(candidateId, dto);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Candidate profile updated successfully",
//                        profile
//                ));
//    }
//
//    // ===========================
//    // Education
//    // ===========================
//
//    @PostMapping("/education")
//    public ResponseEntity<ApiResponse<String>> saveEducation(
//            @RequestBody CandidateEducationDto dto) {
//
//        candidateService.saveEducation(dto);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new ApiResponse<>(
//                        true,
//                        "Education saved successfully",
//                        (CandidateProfileDto) null
//                ));
//    }
//
//    @PutMapping("/education")
//    public ResponseEntity<ApiResponse<String>> updateEducation(
//            @RequestBody CandidateEducationDto dto) {
//
//        candidateService.updateEducation(dto);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Education updated successfully",
//                        (CandidateProfileDto) null
//                ));
//    }
//
//    @GetMapping("/education/{candidateId}")
//    public ResponseEntity<ApiResponse<CandidateEducationDto>> getEducationByCandidateId(
//            @PathVariable Long candidateId) {
//
//        CandidateEducationDto dto =
//                candidateService.getEducationByCandidateId(candidateId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Education fetched successfully",
//                        dto
//                ));
//    }
//
//    @GetMapping("/education/email")
//    public ResponseEntity<ApiResponse<CandidateEducationDto>> getEducationByEmail(
//            @RequestParam String email) {
//
//        CandidateEducationDto dto =
//                candidateService.getEducationByEmail(email);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Education fetched successfully",
//                        dto
//                ));
//    }
//
//    @GetMapping("/education/search")
//    public ResponseEntity<ApiResponse<List<CandidateEducationDto>>> searchEducation(
//            @RequestParam String keyword) {
//
//        List<CandidateEducationDto> list =
//                candidateService.searchEducation(keyword);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Education search completed",
//                        (CandidateProfileDto) list
//                ));
//    }
//
//    @DeleteMapping("/education/{candidateId}")
//    public ResponseEntity<ApiResponse<String>> deleteEducation(
//            @PathVariable Long candidateId) {
//
//        candidateService.deleteEducation(candidateId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Education deleted successfully",
//                        (CandidateProfileDto) null
//                ));
//    }
//
//}