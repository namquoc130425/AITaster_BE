package com.example.AiTaster.service;

import com.example.AiTaster.exception.GlobalException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final String ROOT =
            "uploads/service-files";

    public String saveFile(MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return null;
            }

            Files.createDirectories(
                    Path.of(ROOT)
            );

            String filename =
                    UUID.randomUUID()
                            + "_"
                            + file.getOriginalFilename();

            Path path =
                    Path.of(ROOT, filename);

            Files.copy(
                    file.getInputStream(),
                    path
            );

            return "/uploads/service-files/"
                    + filename;

        } catch (Exception e) {

            throw new GlobalException(
                    500,
                    "Cannot save file"
            );
        }
    }
}
