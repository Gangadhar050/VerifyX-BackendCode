//package com.verify_x.mapper;
//
//import com.verify_x.dto.DocumentResponseDto;
//import org.springframework.stereotype.Component;
//import com.verify_x.entity.Document;
//
//@Component
//public class DocumentMapper {
//
//    public DocumentResponseDto toResponse(Document document){
//
//        if(document == null){
//            return null;
//        }
//
//        return DocumentResponseDto.builder()
//                .id(document.getId())
//                .documentType(document.getDocumentType())
//                .originalFileName(document.getFileName())
//                .contentType(document.getContentType())
//                .fileSize(document.getFileSize())
//                .verificationStatus(document.getVerificationStatus())
//                .remarks(document.getRemarks())
//                .build();
//    }
//
//}
