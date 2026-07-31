package com.verify_x.serviceImpl;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentParserService {

    private static final Pattern ISO_DATE   = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern SLASH_DATE = Pattern.compile("\\b(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4})\\b");
    private static final Pattern LWD_PHRASE = Pattern.compile(
            "last\\s+working\\s+day[:\\s]+([\\d]{4}-[\\d]{2}-[\\d]{2})");

    public DocumentParseResult parse(MultipartFile file) {
        String content = extractContent(file);
        String lower   = content.toLowerCase(Locale.ROOT);

        boolean resignationFound = lower.contains("resign") || lower.contains("resignation");
        boolean acceptanceFound  = lower.contains("accept") || lower.contains("accepted")
                || lower.contains("acceptance") || lower.contains("acknowledge");
        boolean relievingFound   = lower.contains("reliev") || lower.contains("relieving")
                || lower.contains("relieved");

        LocalDate extractedDate = extractLastWorkingDay(lower);

        return new DocumentParseResult(resignationFound, acceptanceFound, relievingFound, extractedDate);
    }

    private String extractContent(MultipartFile file) {
        String contentType = file.getContentType();
        if ("application/pdf".equalsIgnoreCase(contentType)
                || (file.getOriginalFilename() != null
                && file.getOriginalFilename().toLowerCase().endsWith(".pdf"))) {
            return extractPdfText(file);
        }
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private String extractPdfText(MultipartFile file) {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return text == null ? "" : text;
        } catch (IOException e) {
            return "";
        }
    }

    private LocalDate extractLastWorkingDay(String lower) {
        Matcher m = LWD_PHRASE.matcher(lower);
        if (m.find()) {
            try { return LocalDate.parse(m.group(1), DateTimeFormatter.ISO_LOCAL_DATE); }
            catch (DateTimeParseException ignored) {}
        }
        m = ISO_DATE.matcher(lower);
        if (m.find()) {
            try { return LocalDate.parse(m.group(1), DateTimeFormatter.ISO_LOCAL_DATE); }
            catch (DateTimeParseException ignored) {}
        }
        m = SLASH_DATE.matcher(lower);
        if (m.find()) {
            String raw = m.group(1);
            for (String pattern : new String[]{"d/M/yyyy", "d/M/yy", "dd-MM-yyyy", "dd/MM/yyyy"}) {
                try {
                    return LocalDate.parse(raw,
                            DateTimeFormatter.ofPattern(pattern).withLocale(Locale.ENGLISH));
                } catch (DateTimeParseException ignored) {}
            }
        }
        return null;
    }
}
