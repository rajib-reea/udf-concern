package com.udf.microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udf.microservice.domain.ReportDefinition;
import com.udf.microservice.domain.ReportTemplate;
import com.udf.microservice.repository.ReportTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportExecutionService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private DynamicReportService dynamicReportService;

    public ReportTemplate saveTemplate(ReportTemplate template) {
        return reportTemplateRepository.save(template);
    }

    public ReportTemplate updateTemplate(Long id, ReportTemplate template) {
        ReportTemplate existing = reportTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report template not found: " + id));
        existing.setName(template.getName());
        existing.setDescription(template.getDescription());
        existing.setEntityType(template.getEntityType());
        existing.setDefinitionJson(template.getDefinitionJson());
        return reportTemplateRepository.save(existing);
    }

    public void deleteTemplate(Long id) {
        reportTemplateRepository.deleteById(id);
    }

    public Optional<ReportTemplate> findTemplate(Long id) {
        return reportTemplateRepository.findById(id);
    }

    public List<ReportTemplate> findAllTemplates() {
        return reportTemplateRepository.findAll();
    }

    public List<ReportTemplate> findTemplatesByEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return findAllTemplates();
        }
        return reportTemplateRepository.findByEntityType(entityType);
    }

    public ReportDefinition parseDefinition(String json) {
        try {
            return objectMapper.readValue(json, ReportDefinition.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid report definition JSON", e);
        }
    }

    public List<Map<String, Object>> executeTemplate(Long templateId, Map<String, Object> params) {
        ReportTemplate template = findTemplate(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Report template not found: " + templateId));
        ReportDefinition definition = parseDefinition(template.getDefinitionJson());
        applyParameters(definition, params);
        return dynamicReportService.executeReport(definition);
    }

    public String exportTemplate(Long templateId, String format, Map<String, Object> params) {
        List<Map<String, Object>> data = executeTemplate(templateId, params);
        return exportAsFormat(data, format);
    }

    private void applyParameters(ReportDefinition definition, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        // Parameter binding can be extended by replacing placeholder values in filters.
        // Current implementation preserves filters and allows the engine to execute static definitions.
    }

    private String exportAsFormat(List<Map<String, Object>> data, String format) {
        try {
            switch (format.toLowerCase()) {
                case "json":
                    return objectMapper.writeValueAsString(data);
                case "csv":
                    return generateCsv(data);
                case "excel":
                    return "Excel export supported via Apache POI or custom writer";
                default:
                    throw new IllegalArgumentException("Unsupported export format: " + format);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize report data", e);
        }
    }

    private String generateCsv(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "";
        }
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", data.get(0).keySet())).append("\n");
        for (Map<String, Object> row : data) {
            String rowCsv = row.values().stream()
                .map(value -> value != null ? escapeCsv(value.toString()) : "")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            csv.append(rowCsv).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
