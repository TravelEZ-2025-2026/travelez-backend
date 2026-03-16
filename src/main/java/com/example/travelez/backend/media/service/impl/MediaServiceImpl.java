package com.example.travelez.backend.media.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.FileUtils;
import com.example.travelez.backend.infrastructure.filestorage.FileStorageService;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.model.enums.MediaType;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {
    private final FileStorageService fileStorageService;

    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(10);

    private final MediaRepository mediaRepository;

    private final MediaMapper mediaMapper;


    @Override
    @Transactional
    public List<UploadFileResult> uploadFilesParallel(List<MultipartFile> files, String destinationPath) {
        if (files == null || files.isEmpty())
            return Collections.emptyList();

        List<CompletableFuture<UploadFileResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        if (FileUtils.isImage(file)) {
                            return fileStorageService.uploadFile(file, destinationPath, MediaType.IMAGE);
                        } else if (FileUtils.isVideo(file)) {
                            return fileStorageService.uploadFile(file, destinationPath, MediaType.VIDEO);
                        } else {
                            Asserts.fail(ResultCode.BAD_REQUEST, "Unsupported file type: " + file.getOriginalFilename());
                            return null;
                        }
                    } catch (Exception e) {
                        if (e instanceof ApiException) throw (ApiException) e;
                        throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to upload file: " + file.getOriginalFilename());
                    }
                }, uploadExecutor))
                .toList();

        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allDoneFuture.join();
        } catch (Exception e) {
            log.error("Error occurred during upload. Rolling back successful uploads..." + destinationPath);
            List<String> uploadedCloudNames = futures.stream()
                    .filter(f -> !f.isCompletedExceptionally()) // Chỉ lấy cái không lỗi
                    .map(CompletableFuture::join)               // Lấy kết quả
                    .map(UploadFileResult::getCloudName)
                    .collect(Collectors.toList());

            cleanupFilesAsync(uploadedCloudNames);
            Throwable cause = e.getCause();
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Upload failed: " + cause.getMessage());
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Override
    @Async
    public void cleanupFilesAsync(List<String> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty())
            return;
        try {
            fileStorageService.deleteFiles(filesToDelete);
        } catch (Exception e) {
            log.error("Failed to delete files: {}", String.valueOf(e));
        }
    }

    @Override
    @Transactional
    public List<String> processMediaUpdate(List<Media> currentMedias, List<Long> keptMediaIds, List<UploadFileResult> newFiles) {
        List<Media> mediaToDelete = currentMedias.stream()
                .filter(media -> keptMediaIds == null || !keptMediaIds.contains(media.getId()))
                .toList();
        currentMedias.removeAll(mediaToDelete);
        mediaRepository.deleteAll(mediaToDelete);

        if (!newFiles.isEmpty()) {
            List<Media> newMediaEntities = newFiles.stream().map(mediaMapper::toMedia).toList();
            currentMedias.addAll(newMediaEntities);
        }

        return mediaToDelete.stream().map(Media::getCloudName).toList();
    }

    @Override
    public Map<Long, List<Media>> groupMediaByParentId(List<Object[]> rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return Map.of();
        }
        return rawData.stream().collect(Collectors.groupingBy(row -> (Long) row[0], Collectors.mapping(row -> (Media) row[1], Collectors.toList())));
    }
}
