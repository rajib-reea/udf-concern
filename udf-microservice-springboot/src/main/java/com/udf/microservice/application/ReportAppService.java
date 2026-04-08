package com.udf.microservice.application;

import com.udf.microservice.domain.ReportDefinition;
import com.udf.microservice.service.DynamicReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportAppService {

    @Autowired
    private DynamicReportService dynamicReportService;

    public List<Map<String, Object>> executeReport(ReportDefinition reportDefinition) {
        return dynamicReportService.executeReport(reportDefinition);
    }

    public List<Map<String, Object>> executeReportFromJson(String jsonReportDefinition) {
        // Parse JSON to ReportDefinition
        // This would use Jackson or similar
        // For now, return empty list
        return List.of();
    }

    public String exportReport(ReportDefinition reportDefinition, String format) {
        List<Map<String, Object>> data = executeReport(reportDefinition);

        switch (format.toLowerCase()) {
            case "json":
                return exportAsJson(data);
            case "csv":
                return exportAsCsv(data);
            case "excel":
                return exportAsExcel(data);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    private String exportAsJson(List<Map<String, Object>> data) {
        // Use Jackson to convert to JSON
        return "[]"; // Placeholder
    }

    private String exportAsCsv(List<Map<String, Object>> data) {
        if (data.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();

        // Headers
        Map<String, Object> firstRow = data.get(0);
        csv.append(String.join(",", firstRow.keySet())).append("\n");

        // Data
        for (Map<String, Object> row : data) {
            String rowStr = row.values().stream()
                .map(value -> value != null ? value.toString() : "")
                .collect(Collectors.joining(","));
            csv.append(rowStr).append("\n");
        }

        return csv.toString();
    }

    private String exportAsExcel(List<Map<String, Object>> data) {
        // Would use Apache POI or similar
        return "Excel data"; // Placeholder
    }
}