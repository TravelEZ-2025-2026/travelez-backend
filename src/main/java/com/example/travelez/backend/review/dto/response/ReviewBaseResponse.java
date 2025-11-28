package com.example.travelez.backend.review.dto.response;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.review.model.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewBaseResponse extends AuditableEntity {
    private long id;
    private ReviewStatus status;
    private String content;
    private Double rating;
    private String externalName;
    private String externalAvt;
    private Boolean isCrawled;
    private List<MediaBaseResponse> medias;
}
