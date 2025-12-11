package com.example.travelez.backend.infrastructure.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResult {
    private String publicUrl;
    private String cloudName;
}
