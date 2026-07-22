//package com.verify_x.services;
//
//import com.verify_x.dto.DocumentResponseDto;
//import com.verify_x.entity.Document;
//import com.verify_x.enums.DocumentType;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//public interface DocumentService {
//
//    DocumentResponseDto uploadDocument(
//            MultipartFile file,
//            DocumentType documentType,
//            Long employmentId);
//
//    List<DocumentResponseDto> getMyDocuments();
//
//    Document downloadDocument(Long documentId);
//
//    void deleteDocument(Long documentId);
//
//}