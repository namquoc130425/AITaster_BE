package com.example.AiTaster.service;

import com.example.AiTaster.exception.GlobalException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final String SERVICE_FILE_ROOT =
            "uploads/service-files";

    private static final String REPORT_EVIDENCE_ROOT =
            "uploads/report-evidence";

    public String saveFile(MultipartFile file) {
        return saveFileToRoot(
                file,
                SERVICE_FILE_ROOT,
                "/uploads/service-files/"
        );
    }

    public String saveReportEvidence(MultipartFile file) {
        return saveFileToRoot(
                file,
                REPORT_EVIDENCE_ROOT,
                "/uploads/report-evidence/"
        );
    }

    private String saveFileToRoot(
            MultipartFile file,
            String root,
            String publicPrefix
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return null;
            }

            Files.createDirectories(Path.of(root));

            String originalFilename =
                    file.getOriginalFilename();

            String filename =
                    UUID.randomUUID()
                            + "_"
                            + originalFilename;

            Path path =
                    Path.of(root, filename);

            Files.copy(
                    file.getInputStream(),
                    path
            );

            return publicPrefix + filename;

        } catch (Exception e) {
            throw new GlobalException(
                    500,
                    "Cannot save file"
            );
        }
    }
}