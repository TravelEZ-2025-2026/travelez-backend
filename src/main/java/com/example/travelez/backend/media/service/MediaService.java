package com.example.travelez.backend.media.service;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.filestorage.FileStorageService;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final FileStorageService fileStorageService;
 
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final MediaRepository mediaRepository;

    @Transactional
    public List<UploadFileResult> uploadFilesParallel(List<MultipartFile> files, String destinationPath) {
        if (files == null || files.isEmpty())
            return Collections.emptyList();

        List<CompletableFuture<UploadFileResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return fileStorageService.uploadFile(file, destinationPath);
                    } catch (Exception e) {
                        throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR,
                                "Failed to upload file: " + file.getOriginalFilename());
                    }
                })).toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Async
    public void cleanupFilesAsync(List<UploadFileResult> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty())
            return;

        for (UploadFileResult file : filesToDelete) {
            try {
                fileStorageService.deleteFile(file.getCloudName());
            } catch (Exception e) {
                log.error("Failed to delete file: " + file.getCloudName(), e);
            }
        }
    }

}
