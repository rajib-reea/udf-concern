package com.udf.microservice.controller;

import com.udf.microservice.application.ReportAppService;
import com.udf.microservice.domain.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportAppService reportAppService;

    @PostMapping("/execute")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<Map<String, Object>>>> executeReport(
            @RequestBody ReportDefinition reportDefinition) {
        try {
            List<Map<String, Object>> results = reportAppService.executeReport(reportDefinition);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, results, "Report executed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/execute/json")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<Map<String, Object>>>> executeReportFromJson(
            @RequestBody String jsonReportDefinition) {
        try {
            List<Map<String, Object>> results = reportAppService.executeReportFromJson(jsonReportDefinition);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, results, "Report executed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/export/{format}")
    public ResponseEntity<String> exportReport(@RequestBody ReportDefinition reportDefinition,
                                              @PathVariable String format) {
        try {
            String exportedData = reportAppService.exportReport(reportDefinition, format);

            // Set appropriate content type based on format
            String contentType = switch (format.toLowerCase()) {
                case "csv" -> "text/csv";
                case "excel" -> "application/vnd.ms-excel";
                default -> "application/json";
            };

            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=report." + format.toLowerCase())
                .body(exportedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting report: " + e.getMessage());
        }
    }
}