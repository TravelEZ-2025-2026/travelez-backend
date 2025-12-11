package com.example.travelez.backend.infrastructure.filestorage;

import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    UploadFileResult uploadFile(MultipartFile file, String destinationPath);

    void deleteFile(String cloudName);

}
