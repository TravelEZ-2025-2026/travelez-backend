package com.example.travelez.backend.common.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@Builder
public class CommonPage<T> {

    private List<T> content;
    private Integer totalPages;
    private Long totalElements;
    private Integer size;
    private Integer page;
    private boolean empty;

    public CommonPage(List<T> content, Integer totalPages, Long totalElements, Integer size, Integer page, boolean empty) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.page = page;
        this.empty = empty;
    }

    public static <T> CommonPage<T> empty() {
        return new CommonPage<>(List.of(), 0, 0L, 0, 0, true);
    }
}
