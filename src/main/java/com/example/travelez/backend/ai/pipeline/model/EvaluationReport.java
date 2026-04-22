package com.example.travelez.backend.ai.pipeline.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvaluationReport {
    private boolean isPassed;
    private List<String> errors;
}