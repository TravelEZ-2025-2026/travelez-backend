package com.example.travelez.backend.infrastructure.filestorage.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.filestorage.FileStorageService;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsService implements FileStorageService {

    private final Storage storage;
    private final String bucketName;

    @Autowired
    public GcsService(Storage storage, @Value("${gcp.bucket.name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    @Override
    public UploadFileResult uploadFile(MultipartFile file, String destinationPath) {

        String fileName = destinationPath + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // Upload the file to the bucket
        try {
            storage.create(blobInfo, file.getBytes());
            String publicUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;
            return new UploadFileResult(publicUrl, fileName);
        } catch (IOException e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to upload file to GCS: ");
        }
    }

    @Override
    public void deleteFile(String blobName) {
        if (blobName == null || blobName.isEmpty()) {
            return;
        }
        BlobId blobId = BlobId.of(bucketName, blobName);
        storage.delete(blobId);
    }
}
