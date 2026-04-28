package com.example.travelez.backend.posts.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.travelez.backend.posts.model.enums.PostStatus;

import lombok.Builder;
import lombok.Data;

@Data
public class AdminPostsFilterRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate toDate;

    private List<PostStatus> statuses;

    private String topicTag;

    private Long poiId;
}
