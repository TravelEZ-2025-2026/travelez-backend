package com.example.travelez.backend.posts.dto.request;

import lombok.Data;
import lombok.Builder.Default;

import org.springframework.format.annotation.DateTimeFormat;

import com.google.auto.value.AutoValue.Builder;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Data
public class PostsSearchRequest {

    @NotBlank(message = "Keyword is required")
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate toDate;

    private int size = 4;

    private int page = 0;
}
