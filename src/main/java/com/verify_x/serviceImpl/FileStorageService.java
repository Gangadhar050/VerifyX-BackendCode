//package com.verify_x.serviceImpl;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.UUID;
//
//@Component
//public class FileStorageService {
//
//    @Value("${app.upload.base-dir:uploads}")
//    private String baseDir;
//
//    /**
//     * Stores a multipart file under {baseDir}/{subFolder}/ and returns the relative path saved.
//     * subFolder example: "notice-period/42" or "salary-hold/42" (userProfileId scoped).
//     */
//    public String store(MultipartFile file, String subFolder) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("Uploaded file is empty");
//        }
//
//        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null
//                ? "file" : file.getOriginalFilename());
//        String extension = "";
//        int dotIndex = originalName.lastIndexOf('.');
//        if (dotIndex >= 0) {
//            extension = originalName.substring(dotIndex);
//        }
//        String storedFileName = UUID.randomUUID() + extension;
//
//        try {
//            Path targetDir = Paths.get(baseDir, subFolder).normalize();
//            Files.createDirectories(targetDir);
//
//            Path targetPath = targetDir.resolve(storedFileName).normalize();
//            if (!targetPath.startsWith(targetDir)) {
//                throw new IOException("Invalid file path resolution");
//            }
//
//            try (InputStream in = file.getInputStream()) {
//                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
//            }
//
//            return targetPath.toString();
//        } catch (IOException ex) {
//            throw new RuntimeException("Failed to store file: " + originalName, ex);
//        }
//    }
//
//}