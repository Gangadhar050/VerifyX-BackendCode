//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.DocumentResponseDto;
//import com.verify_x.entity.Document;
//import com.verify_x.entity.EmploymentHistory;
//import com.verify_x.entity.Screening;
//import com.verify_x.entity.User;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.enums.VerificationStatus;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.mapper.DocumentMapper;
//import com.verify_x.repository.DocumentRepository;
//import com.verify_x.repository.EmploymentHistoryRepository;
//import com.verify_x.repository.ScreeningRepository;
//import com.verify_x.services.*;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
////@Transactional
//public class DocumentServiceImpl implements DocumentService {
//
//    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
//
//    private final DocumentRepository documentRepository;
//    private final EmploymentHistoryRepository employmentRepository;
//    private final ScreeningRepository screeningRepository;
//    private final DocumentMapper documentMapper;
//    private final CurrentUserService currentUserService;
//    private final RelievingLetterService relievingLetterService;
//    private final OfferLetterService offerLetterService;
//    private final UANNumberService uanNumberService;
//    private final ResumeService resumeService;
//
//    @Override
//    public DocumentResponseDto uploadDocument(
//            MultipartFile file,
//            DocumentType documentType,
//            Long employmentId) {
//
//        validateFile(file);
//
//        User user = currentUserService.getCurrentUser();
//
//        Screening screening = screeningRepository.findByUser(user)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Screening not found."));
//
//        Document document = new Document();
//
//        document.setUserId(user.getId());
//        document.setDocumentType(documentType);
//        document.setFileName(file.getOriginalFilename());
//        document.setContentType(file.getContentType());
//        document.setFileSize(file.getSize());
//        document.setVerificationStatus(
//                VerificationStatus.PENDING);
//
//
//
//        try {
//
//            document.setFileData(file.getBytes());
//
//        } catch (IOException e) {
//
//            throw new BadRequestException(
//                    "Unable to read uploaded file.");
//
//        }
//
//        if (employmentId != null) {
//
//            EmploymentHistory employment =
//                    employmentRepository.findById(employmentId)
//                            .orElseThrow(() ->
//                                    new ResourceNotFoundException(
//                                            "Employment history not found."));
//
//            if (!employment.getScreening()
//                    .getId()
//                    .equals(screening.getId())) {
//
//                throw new BadRequestException(
//                        "Employment does not belong to logged-in user.");
//            }
//
////            document.setEmploymentHistory(employment);
//
//        } else {
//
//            document.setScreening(screening);
//
//        }
//
//        Document savedDoc = documentRepository.save(document);
//
//        String enteredText = "NA";
//        switch (documentType) {
//            case EXPERIENCE_LETTER:
//                System.out.println("EXPERIENCE_LETTER");
//                break;
//            case RELIEVING_LETTER:
//                relievingLetterService.uploadRelievingLetter(file, enteredText, documentType);
//                break;
//            case OFFER_LETTER:
//                offerLetterService.uploadOfferLetter(file,enteredText, documentType);
//                break;
//            case EPF_PASSBOOK:
//                uanNumberService.uploadUAN(file, enteredText);
//                break;
//
//            case RESUME:
//                resumeService.uploadResume(file,document, documentType);
//                break;
//
//            case HIGHER_EDUCATION:
//                System.out.println("HIGHER_EDUCATION");
//                break;
//
//            case SALARY_SLIP:
//                System.out.println("SALARY_SLIP");
//                break;
//            case CERTIFICATION:
//                System.out.println("CERTIFICATION");
//                break;
//
//            case AADHAAR_CARD:
//                System.out.println("AADHAAR_CARD");
//                break;
//            case PAN_CARD:
//                System.out.println("PAN_CARD");
//                break;
//            case OTHER:
//                System.out.println("OTHER");
//                break;
//        }
//        return documentMapper.toResponse(savedDoc);
//    }
//
//    private void validateFile(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//
//            throw new BadRequestException(
//                    "Please select a file.");
//
//        }
//
//        if (file.getSize() > MAX_FILE_SIZE) {
//
//            throw new BadRequestException(
//                    "Maximum file size is 5 MB.");
//
//        }
//
//        String contentType = file.getContentType();
//
//        if (contentType == null ||
//                !(contentType.equals("application/pdf")
//                        || contentType.equals("image/jpeg")
//                        || contentType.equals("image/png"))) {
//
//            throw new BadRequestException(
//                    "Only PDF, JPG and PNG files are allowed.");
//
//        }
//
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DocumentResponseDto> getMyDocuments() {
//
//        User user = currentUserService.getCurrentUser();
//
//        Screening screening = screeningRepository.findByUser(user)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Screening not found."));
//
//        List<Document> documents =
//                documentRepository.findByScreening(screening);
//
////        List<EmploymentHistory> employments =
////                employmentRepository.findByScreening(screening);
//
////        employments.forEach(employment ->
////                documents.addAll(
////                        documentRepository.findByEmploymentHistory(employment)
////                ));
//
//        return documents.stream()
//                .map(documentMapper::toResponse)
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Document downloadDocument(Long documentId) {
//
//        User user = currentUserService.getCurrentUser();
//
//        Screening screening = screeningRepository.findByUser(user)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Screening not found."));
//
//        Document document = documentRepository.findById(documentId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Document not found."));
//
//        // Gap Document
//        if (document.getScreening() != null) {
//
//            if (!document.getScreening().getId()
//                    .equals(screening.getId())) {
//
//                throw new BadRequestException(
//                        "You cannot access another user's document.");
//            }
//
//            return document;
//        }
//
//        throw new ResourceNotFoundException("Document not found.");
//    }
//
//    @Override
//    public void deleteDocument(Long documentId) {
//
//        User user = currentUserService.getCurrentUser();
//
//        Screening screening = screeningRepository.findByUser(user)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Screening not found."));
//
//        Document document = documentRepository.findById(documentId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Document not found."));
//
//        // Gap Document
//        if (document.getScreening() != null) {
//
//            if (!document.getScreening().getId()
//                    .equals(screening.getId())) {
//
//                throw new BadRequestException(
//                        "You cannot delete another user's document.");
//            }
//
//            documentRepository.delete(document);
//            return;
//        }
//
//        throw new ResourceNotFoundException("Document not found.");
//    }
//
//}
