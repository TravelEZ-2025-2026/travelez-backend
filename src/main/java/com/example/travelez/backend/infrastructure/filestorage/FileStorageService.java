package com.example.travelez.backend.infrastructure.filestorage;

import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.model.enums.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    UploadFileResult uploadFile(MultipartFile file, String destinationPath, MediaType mediaType);

    void deleteFile(String cloudName);

    void deleteFiles(List<String> cloudNames);

}
