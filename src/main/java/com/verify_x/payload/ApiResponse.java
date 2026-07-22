package com.verify_x.payload;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.CandidateEducationDto;
import com.verify_x.dto.CandidateProfileDto;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timeStamp;

    public ApiResponse(boolean b, String candidateProfileFetchedSuccessfully, CandidateProfileDto profile) {
    }

    public ApiResponse(boolean b, String educationFetchedSuccessfully, CandidateEducationDto dto) {
    }

    public ApiResponse(boolean b, String s, CandidateDocumentDto document) {
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
}
