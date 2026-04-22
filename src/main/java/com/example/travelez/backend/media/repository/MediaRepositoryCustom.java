package com.example.travelez.backend.media.repository;

import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.model.Media;

import java.util.List;

public interface MediaRepositoryCustom {
    List<Media> batchInsertMedias(List<Media> medias);

    void batchInsertMediaRelation(MediaTarget target, Long entityId, List<Long> mediaIds);
}
