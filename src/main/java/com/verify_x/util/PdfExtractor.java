package com.verify_x.util;


import java.io.IOException;

import com.verify_x.exception.PdfProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class PdfExtractor {


    public String extractText(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new PdfProcessingException(
                    "Uploaded PDF file is empty.");
        }

        try {

            return extractText(file.getBytes());

        } catch (IOException e) {

            throw new PdfProcessingException(
                    "Unable to read uploaded PDF file : "
                            + e.getMessage());
        }
    }


    public String extractText(byte[] documentData) {

        if (documentData == null || documentData.length == 0) {

            throw new PdfProcessingException(
                    "Document data is empty.");
        }

        try (PDDocument document = Loader.loadPDF(documentData)) {

            PDFTextStripper pdfTextStripper =
                    new PDFTextStripper();

            String extractedText =
                    pdfTextStripper.getText(document);

            if (extractedText == null) {

                throw new PdfProcessingException(
                        "Unable to extract text from PDF.");
            }

            extractedText = extractedText.trim();

            if (extractedText.isEmpty()) {

                throw new PdfProcessingException(
                        "No text found in PDF.");
            }

            return extractedText;

        } catch (IOException e) {

            throw new PdfProcessingException(
                    "Failed to process PDF : "
                            + e.getMessage());

        } catch (Exception e) {

            throw new PdfProcessingException(
                    "Unexpected error while extracting PDF : "
                            + e.getMessage());
        }
    }
}
//import java.io.IOException;
//
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.verify_x.exception.PdfProcessingException;
//
//@Component
//public class PdfExtractor {
//
//    public String extractText(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//            throw new PdfProcessingException("PDF file is empty.");
//        }
//
//        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
//
//            PDFTextStripper stripper = new PDFTextStripper();
//
//            String text = stripper.getText(document);
//
//
//            if (text == null || text.trim().isEmpty()) {
//                throw new PdfProcessingException("No text found in PDF.");
//            }
//
//            return text.trim();
//
//        } catch (IOException e) {
//
//            throw new PdfProcessingException(
//                    "Unable to process PDF file: " + e.getMessage());
//
//        } catch (Exception e) {
//
//            throw new PdfProcessingException(
//                    "Unexpected error while reading PDF: " + e.getMessage());
//        }
//    }
//}