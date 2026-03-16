package com.example.travelez.backend.infrastructure.filestorage.dto;

import com.example.travelez.backend.media.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResult {
    private String publicUrl;
    private String cloudName;
    private MediaType mediaType;
}
