package com.example.travelez.backend.report.dto.request;

import com.example.travelez.backend.report.model.enums.ReportReason;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReportCreateRequest {
    @NotNull(message = "Target type is required")
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Reason is required")
    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String reasonDetail;

    private List<MultipartFile> files;
}
