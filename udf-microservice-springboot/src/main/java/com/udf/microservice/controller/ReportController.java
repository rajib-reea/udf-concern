package com.udf.microservice.controller;

import com.udf.microservice.application.ReportAppService;
import com.udf.microservice.domain.ReportDefinition;
import com.udf.microservice.domain.ReportTemplate;
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

    @PostMapping("/execute/visual")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<Map<String, Object>>>> executeVisualQuery(
            @RequestBody ReportAppService.VisualQueryRequest visualQuery) {
        try {
            List<Map<String, Object>> results = reportAppService.executeVisualQuery(visualQuery);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, results, "Visual query executed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/export/visual/{format}")
    public ResponseEntity<String> exportVisualQuery(@RequestBody ReportAppService.VisualQueryRequest visualQuery,
                                                   @PathVariable String format) {
        try {
            String exportedData = reportAppService.exportVisualQuery(visualQuery, format);
            String contentType = resolveContentType(format);
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=visual-report." + format.toLowerCase())
                .body(exportedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting visual query: " + e.getMessage());
        }
    }

    @PostMapping("/execute/template/{id}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<Map<String, Object>>>> executeTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> parameters) {
        try {
            List<Map<String, Object>> results = reportAppService.executeTemplate(id, parameters);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, results, "Template executed successfully"));
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
            String contentType = resolveContentType(format);
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=report." + format.toLowerCase())
                .body(exportedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting report: " + e.getMessage());
        }
    }

    @PostMapping("/export/template/{id}/{format}")
    public ResponseEntity<String> exportTemplate(@PathVariable Long id,
                                                 @PathVariable String format,
                                                 @RequestBody(required = false) Map<String, Object> parameters) {
        try {
            String exportedData = reportAppService.exportTemplate(id, format, parameters);
            String contentType = resolveContentType(format);
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=template-report." + format.toLowerCase())
                .body(exportedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting report template: " + e.getMessage());
        }
    }

    @GetMapping("/definitions")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<ReportTemplate>>> getReportTemplates(
            @RequestParam(required = false) String entityType) {
        try {
            List<ReportTemplate> templates = reportAppService.getTemplates(entityType);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, templates, "Report templates retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/definitions/{id}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<ReportTemplate>> getReportTemplate(@PathVariable Long id) {
        try {
            var templateOpt = reportAppService.getTemplate(id);
            if (templateOpt.isPresent()) {
                return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, templateOpt.get(), "Report template retrieved successfully"));
            }
            return ResponseEntity.<UdfDefinitionController.ApiResponse<ReportTemplate>>notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/definitions")
    public ResponseEntity<UdfDefinitionController.ApiResponse<ReportTemplate>> createReportTemplate(
            @RequestBody ReportTemplate template) {
        try {
            ReportTemplate saved = reportAppService.createTemplate(template);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, saved, "Report template created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/definitions/{id}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<ReportTemplate>> updateReportTemplate(
            @PathVariable Long id,
            @RequestBody ReportTemplate template) {
        try {
            ReportTemplate updated = reportAppService.updateTemplate(id, template);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, updated, "Report template updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @DeleteMapping("/definitions/{id}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<Void>> deleteReportTemplate(@PathVariable Long id) {
        try {
            reportAppService.deleteTemplate(id);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, null, "Report template deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    private String resolveContentType(String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return "text/csv";
            case "excel":
                return "application/vnd.ms-excel";
            default:
                return "application/json";
        }
    }
}
