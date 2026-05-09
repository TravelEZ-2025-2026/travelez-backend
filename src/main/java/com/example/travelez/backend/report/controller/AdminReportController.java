package com.example.travelez.backend.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;
import com.example.travelez.backend.report.service.AdminReportService;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Reports", description = "Admin Reports endpoints")
public class AdminReportController {
    private final AdminReportService adminReportService;

    // @GetMapping("/list")
    // public ResponseEntity<BaseResponse<CommonPage<ReportResponse>>> getList(@ParameterObject ReportFilterRequest request,
    //                                                                         @RequestParam(required = false, defaultValue = "id") String sortField,
    //                                                                         @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
    //                                                                         @RequestParam(required = false, defaultValue = "0") Integer page,
    //                                                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
    //     return BaseResponse.success(adminReportService.getList(request, PaginationUtils.getPageable(page, size)), ResultCode.SUCCESS, "Reports fetched successfully");
    // }

    @PostMapping("/reject")
    public ResponseEntity<BaseResponse<Void>> rejectReport(@RequestBody @Valid AdminRejectReportRequest request) {
        adminReportService.rejectReports(request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Report rejected successfully");
    }

}
