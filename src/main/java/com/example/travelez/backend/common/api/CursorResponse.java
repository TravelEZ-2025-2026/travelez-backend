package com.example.travelez.backend.common.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class CursorResponse<T> {
    private List<T> content;
    private String nextCursor;
    private boolean hasNext;

    public CursorResponse(List<T> content, String nextCursor, boolean hasNext) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
