package com.example.travelez.backend.report.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.travelez.backend.report.handler.ReportHandler;
import com.example.travelez.backend.report.model.enums.ReportTargetType;

@Configuration
public class ReportConfig {
    @Bean
    public Map<ReportTargetType, ReportHandler> reportHandlerMap(List<ReportHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(ReportHandler::getSupportedType, Function.identity()));
    }
}
