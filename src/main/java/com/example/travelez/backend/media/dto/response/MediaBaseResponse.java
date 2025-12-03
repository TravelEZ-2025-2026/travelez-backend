package com.example.travelez.backend.media.dto.response;

import com.example.travelez.backend.media.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaBaseResponse {
    private long id;
    private String url;
    private MediaType type;
}
