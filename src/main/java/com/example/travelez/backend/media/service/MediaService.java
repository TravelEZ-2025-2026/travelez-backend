package com.example.travelez.backend.media.service;

import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.model.Media;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MediaService {

    List<UploadFileResult> uploadFilesParallel(List<MultipartFile> files, String destinationPath);

    // upload 1 file len cloud storage
    UploadFileResult uploadFile(MultipartFile file, String destinationPath);

    void cleanupFilesAsync(List<String> filesToDelete);

    //    dung de xu ly cap nhat media cho entity (review, post, etc) trong db, tra ve danh sach publicUrl can xoa tren cloud storage
    List<String> processMediaUpdate(List<Media> currentMedias, List<Long> keptMediaIds, List<UploadFileResult> newFiles);

    Map<Long, List<Media>> groupMediaByParentId(List<Object[]> rawData);

    List<Media> attachMediasToEntity(List<UploadFileResult> uploadFiles, MediaTarget target, Long entityId);
}
