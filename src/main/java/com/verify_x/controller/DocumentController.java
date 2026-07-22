//package com.verify_x.controller;
//
//import com.verify_x.dto.DocumentResponseDto;
//import com.verify_x.entity.Document;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.payload.ApiResponse;
//import com.verify_x.services.DocumentService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/documents")
//@RequiredArgsConstructor
//@Tag(name = "Document")
//public class DocumentController {
//
//    private final DocumentService documentService;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(summary = "Upload Document")
//    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
//
//            @RequestParam MultipartFile file,
//
//            @RequestParam DocumentType documentType,
//
//            @RequestParam(required = false) Long employmentId) {
//
//        DocumentResponseDto response =
//                documentService.uploadDocument(
//                        file,
//                        documentType,
//                        employmentId);
//
//        return ResponseEntity.ok(
//                ApiResponse.<DocumentResponseDto>builder()
//                        .success(true)
//                        .message("Document uploaded successfully.")
//                        .data(response)
//                        .build()
//        );
//    }
//
//    @GetMapping
//    @Operation(summary = "Get My Documents")
//    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getMyDocuments() {
//
//        return ResponseEntity.ok(
//                ApiResponse.<List<DocumentResponseDto>>builder()
//                        .success(true)
//                        .message("Documents fetched successfully.")
//                        .data(documentService.getMyDocuments())
//                        .build()
//        );
//    }
//
//    @GetMapping("/{documentId}")
//    @Operation(summary = "Download Document")
//    public ResponseEntity<byte[]> downloadDocument(
//            @PathVariable Long documentId) {
//
//        Document document = documentService.downloadDocument(documentId);
//
//        return ResponseEntity.ok()
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + document.getFileName() + "\""
//                )
//                .contentType(MediaType.parseMediaType(document.getContentType()))
//                .body(document.getFileData());
//    }
//
//    @DeleteMapping("/{documentId}")
//    @Operation(summary = "Delete Document")
//    public ResponseEntity<ApiResponse<String>> deleteDocument(
//            @PathVariable Long documentId) {
//
//        documentService.deleteDocument(documentId);
//
//        return ResponseEntity.ok(
//                ApiResponse.<String>builder()
//                        .success(true)
//                        .message("Document deleted successfully.")
//                        .data("Deleted")
//                        .build()
//        );
//    }
//
//}
