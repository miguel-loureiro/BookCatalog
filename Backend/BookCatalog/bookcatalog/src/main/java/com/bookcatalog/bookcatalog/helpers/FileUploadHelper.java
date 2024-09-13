package com.bookcatalog.bookcatalog.helpers;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadHelper {
    public static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    public static String saveFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds 2MB limit");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.write(filePath, file.getBytes());

        return fileName;
    }
}
