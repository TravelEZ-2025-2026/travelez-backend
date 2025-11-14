package com.example.travelez.backend.common.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@Builder
public class CommonPage<T> {

    private List<T> list;
    private Integer totalPages;
    private Long totalElements;
    private Integer size;
    private Integer page;

    public CommonPage(List<T> list, Integer totalPages, Long totalElements, Integer size, Integer page) {
        this.list = list;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.page = page;
    }
}
