package com.example.travelez.backend.common.utils;

import com.example.travelez.backend.common.dto.PaginationRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaginationUtils {

    public static Pageable getPageable(PaginationRequest request) {
        return PageRequest.of(request.getPage(), request.getSize(), request.getDirection(), request.getSortField());
    }

    public static Pageable getPageable(int page, int size) {
        return PageRequest.of(page, size);
    }
}

