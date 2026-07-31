package com.verify_x.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CandidateDocumentRequest {

    private MultipartFile resume;

    private MultipartFile offerLetter;

    private MultipartFile salarySlip;

    private MultipartFile relievingLetter;

    private MultipartFile experienceLetter;

    private MultipartFile panCard;

    private MultipartFile uanProof;

}