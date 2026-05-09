package com.example.travelez.backend.report.event;

import java.util.List;

import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportProcessedEvent {
    private List<Report> processedReports;
    private ReportStatus newStatus;
}
