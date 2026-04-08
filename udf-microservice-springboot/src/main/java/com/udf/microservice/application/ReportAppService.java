package com.udf.microservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udf.microservice.domain.ReportDefinition;
import com.udf.microservice.domain.ReportTemplate;
import com.udf.microservice.service.DynamicReportService;
import com.udf.microservice.service.ReportExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportAppService {

    @Autowired
    private DynamicReportService dynamicReportService;

    @Autowired
    private ReportExecutionService reportExecutionService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Map<String, Object>> executeReport(ReportDefinition reportDefinition) {
        return dynamicReportService.executeReport(reportDefinition);
    }

    public List<Map<String, Object>> executeReportFromJson(String jsonReportDefinition) {
        try {
            ReportDefinition reportDefinition = objectMapper.readValue(jsonReportDefinition, ReportDefinition.class);
            return executeReport(reportDefinition);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid report definition JSON", e);
        }
    }

    public List<Map<String, Object>> executeVisualQuery(VisualQueryRequest visualQuery) {
        ReportDefinition reportDef = convertVisualQueryToReportDefinition(visualQuery);
        return executeReport(reportDef);
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

    public String exportVisualQuery(VisualQueryRequest visualQuery, String format) {
        ReportDefinition reportDef = convertVisualQueryToReportDefinition(visualQuery);
        return exportReport(reportDef, format);
    }

    public ReportTemplate createTemplate(ReportTemplate template) {
        return reportExecutionService.saveTemplate(template);
    }

    public ReportTemplate updateTemplate(Long id, ReportTemplate template) {
        return reportExecutionService.updateTemplate(id, template);
    }

    public void deleteTemplate(Long id) {
        reportExecutionService.deleteTemplate(id);
    }

    public Optional<ReportTemplate> getTemplate(Long id) {
        return reportExecutionService.findTemplate(id);
    }

    public List<ReportTemplate> getTemplates(String entityType) {
        return reportExecutionService.findTemplatesByEntityType(entityType);
    }

    public List<Map<String, Object>> executeTemplate(Long templateId, Map<String, Object> params) {
        return reportExecutionService.executeTemplate(templateId, params);
    }

    public String exportTemplate(Long templateId, String format, Map<String, Object> params) {
        return reportExecutionService.exportTemplate(templateId, format, params);
    }

    private ReportDefinition convertVisualQueryToReportDefinition(VisualQueryRequest visualQuery) {
        List<ReportDefinition.Aggregation> aggregations = visualQuery.getAggregations() != null ?
            visualQuery.getAggregations().stream()
                .map(agg -> new ReportDefinition.Aggregation(agg.getField(), agg.getFunction()))
                .collect(Collectors.toList()) : null;

        List<ReportDefinition.Filter> filters = visualQuery.getFilters() != null ?
            visualQuery.getFilters().stream()
                .map(f -> new ReportDefinition.Filter(f.getField(), f.getOperator(), f.getValue()))
                .collect(Collectors.toList()) : null;

        List<ReportDefinition.Sort> sorts = visualQuery.getSorts() != null ?
            visualQuery.getSorts().stream()
                .map(s -> new ReportDefinition.Sort(s.getField(), s.getDirection()))
                .collect(Collectors.toList()) : null;

        return new ReportDefinition(
            visualQuery.getEntityType(),
            visualQuery.getColumns(),
            aggregations,
            filters,
            visualQuery.getGroupBy(),
            sorts,
            visualQuery.getLimit()
        );
    }

    private String exportAsJson(List<Map<String, Object>> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize report data to JSON", e);
        }
    }

    private String exportAsCsv(List<Map<String, Object>> data) {
        if (data.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();

        Map<String, Object> firstRow = data.get(0);
        csv.append(String.join(",", firstRow.keySet())).append("\n");

        for (Map<String, Object> row : data) {
            String rowStr = row.values().stream()
                .map(value -> value != null ? escapeCsv(value.toString()) : "")
                .collect(Collectors.joining(","));
            csv.append(rowStr).append("\n");
        }

        return csv.toString();
    }

    private String exportAsExcel(List<Map<String, Object>> data) {
        return "Excel export will be supported by a dedicated writer such as Apache POI.";
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // DTO Classes for Visual Query Builder
    public static class VisualQueryRequest {
        private String entityType;
        private List<String> columns;
        private List<VisualAggregation> aggregations;
        private List<VisualFilter> filters;
        private List<String> groupBy;
        private List<VisualSort> sorts;
        private Integer limit;

        public VisualQueryRequest() {}

        public VisualQueryRequest(String entityType, List<String> columns, List<VisualAggregation> aggregations,
                                List<VisualFilter> filters, List<String> groupBy, List<VisualSort> sorts, Integer limit) {
            this.entityType = entityType;
            this.columns = columns;
            this.aggregations = aggregations;
            this.filters = filters;
            this.groupBy = groupBy;
            this.sorts = sorts;
            this.limit = limit;
        }

        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }

        public List<VisualAggregation> getAggregations() { return aggregations; }
        public void setAggregations(List<VisualAggregation> aggregations) { this.aggregations = aggregations; }

        public List<VisualFilter> getFilters() { return filters; }
        public void setFilters(List<VisualFilter> filters) { this.filters = filters; }

        public List<String> getGroupBy() { return groupBy; }
        public void setGroupBy(List<String> groupBy) { this.groupBy = groupBy; }

        public List<VisualSort> getSorts() { return sorts; }
        public void setSorts(List<VisualSort> sorts) { this.sorts = sorts; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }

    public static class VisualAggregation {
        private String field;
        private String function;

        public VisualAggregation() {}

        public VisualAggregation(String field, String function) {
            this.field = field;
            this.function = function;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getFunction() { return function; }
        public void setFunction(String function) { this.function = function; }
    }

    public static class VisualFilter {
        private String field;
        private String operator;
        private Object value;

        public VisualFilter() {}

        public VisualFilter(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class VisualSort {
        private String field;
        private String direction;

        public VisualSort() {}

        public VisualSort(String field, String direction) {
            this.field = field;
            this.direction = direction;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }
}