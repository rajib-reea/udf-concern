package com.udf.microservice.service;

import com.udf.microservice.domain.ReportDefinition;
import com.udf.microservice.domain.ReportableField;
import com.udf.microservice.repository.ReportableFieldRepository;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DynamicReportService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReportableFieldRepository reportableFieldRepository;

    public List<Map<String, Object>> executeReport(ReportDefinition reportDef) {
        // Validate the report definition
        validateReportDefinition(reportDef);

        // Create DSL context
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRES);

        // Build the query
        SelectQuery<?> query = create.selectQuery();

        // Add table
        Table<?> table = DSL.table(DSL.name(reportDef.getEntity()));
        query.addFrom(table);

        // Add columns and aggregations
        addColumnsAndAggregations(query, reportDef, table);

        // Add filters
        addFilters(query, reportDef, table);

        // Add group by
        addGroupBy(query, reportDef, table);

        // Add order by
        addOrderBy(query, reportDef, table);

        // Add limit
        if (reportDef.getLimit() != null && reportDef.getLimit() > 0) {
            query.addLimit(reportDef.getLimit());
        }

        // Execute and return results
        return query.fetch().intoMaps();
    }

    private void validateReportDefinition(ReportDefinition reportDef) {
        if (reportDef.getEntity() == null || reportDef.getEntity().trim().isEmpty()) {
            throw new IllegalArgumentException("Entity must be specified");
        }

        // Validate that all fields exist in metadata
        List<ReportableField> availableFields = reportableFieldRepository.findByEntity(reportDef.getEntity());
        Map<String, ReportableField> fieldMap = availableFields.stream()
            .collect(Collectors.toMap(ReportableField::getFieldName, f -> f));

        // Check columns
        if (reportDef.getColumns() != null) {
            for (String column : reportDef.getColumns()) {
                if (!fieldMap.containsKey(column)) {
                    throw new IllegalArgumentException("Unknown column: " + column);
                }
            }
        }

        // Check aggregations
        if (reportDef.getAggregations() != null) {
            for (ReportDefinition.Aggregation agg : reportDef.getAggregations()) {
                if (!fieldMap.containsKey(agg.getField())) {
                    throw new IllegalArgumentException("Unknown field in aggregation: " + agg.getField());
                }
                if (!fieldMap.get(agg.getField()).isAggregatable()) {
                    throw new IllegalArgumentException("Field not aggregatable: " + agg.getField());
                }
            }
        }

        // Check filters
        if (reportDef.getFilters() != null) {
            for (ReportDefinition.Filter filter : reportDef.getFilters()) {
                if (!fieldMap.containsKey(filter.getField())) {
                    throw new IllegalArgumentException("Unknown field in filter: " + filter.getField());
                }
                if (!fieldMap.get(filter.getField()).isFilterable()) {
                    throw new IllegalArgumentException("Field not filterable: " + filter.getField());
                }
            }
        }

        // Check group by
        if (reportDef.getGroupBy() != null) {
            for (String groupField : reportDef.getGroupBy()) {
                if (!fieldMap.containsKey(groupField)) {
                    throw new IllegalArgumentException("Unknown field in group by: " + groupField);
                }
            }
        }

        // Check sort
        if (reportDef.getSort() != null) {
            for (ReportDefinition.Sort sort : reportDef.getSort()) {
                if (!fieldMap.containsKey(sort.getField())) {
                    throw new IllegalArgumentException("Unknown field in sort: " + sort.getField());
                }
            }
        }
    }

    private void addColumnsAndAggregations(SelectQuery<?> query, ReportDefinition reportDef, Table<?> table) {
        List<ReportableField> availableFields = reportableFieldRepository.findByEntity(reportDef.getEntity());
        Map<String, ReportableField> fieldMap = availableFields.stream()
            .collect(Collectors.toMap(ReportableField::getFieldName, f -> f));

        // Add regular columns
        if (reportDef.getColumns() != null) {
            for (String column : reportDef.getColumns()) {
                ReportableField field = fieldMap.get(column);
                Field<?> fieldObj = DSL.field(DSL.name(field.getSourceColumn()));
                query.addSelect(fieldObj.as(column));
            }
        }

        // Add aggregations
        if (reportDef.getAggregations() != null) {
            for (ReportDefinition.Aggregation agg : reportDef.getAggregations()) {
                ReportableField field = fieldMap.get(agg.getField());
                Field<?> fieldObj = DSL.field(DSL.name(field.getSourceColumn()));
                Field<?> aggField = createAggregationField(fieldObj, agg.getFunc(), agg.getField());
                query.addSelect(aggField);
            }
        }

        // If no columns or aggregations specified, select all fields
        if ((reportDef.getColumns() == null || reportDef.getColumns().isEmpty()) &&
            (reportDef.getAggregations() == null || reportDef.getAggregations().isEmpty())) {
            query.addSelect(DSL.asterisk());
        }
    }

    private Field<?> createAggregationField(Field<?> field, String func, String alias) {
        switch (func.toUpperCase()) {
            case "SUM":
                return field.sum().as(alias + "_sum");
            case "AVG":
                return field.avg().as(alias + "_avg");
            case "COUNT":
                return field.count().as(alias + "_count");
            case "MIN":
                return field.min().as(alias + "_min");
            case "MAX":
                return field.max().as(alias + "_max");
            default:
                throw new IllegalArgumentException("Unsupported aggregation function: " + func);
        }
    }

    private void addFilters(SelectQuery<?> query, ReportDefinition reportDef, Table<?> table) {
        if (reportDef.getFilters() == null || reportDef.getFilters().isEmpty()) {
            return;
        }

        List<ReportableField> availableFields = reportableFieldRepository.findByEntity(reportDef.getEntity());
        Map<String, ReportableField> fieldMap = availableFields.stream()
            .collect(Collectors.toMap(ReportableField::getFieldName, f -> f));

        Condition condition = DSL.trueCondition();

        for (ReportDefinition.Filter filter : reportDef.getFilters()) {
            ReportableField field = fieldMap.get(filter.getField());
            Field<?> fieldObj = DSL.field(DSL.name(field.getSourceColumn()));
            Condition filterCondition = buildFilterCondition(fieldObj, filter);
            condition = condition.and(filterCondition);
        }

        query.addConditions(condition);
    }

    @SuppressWarnings("unchecked")
    private Condition buildFilterCondition(Field<?> field, ReportDefinition.Filter filter) {
        Object value = filter.getValue();
        Field<Object> objectField = (Field<Object>) field;

        switch (filter.getOp().toLowerCase()) {
            case "=":
            case "eq":
                return objectField.eq(DSL.val(value));
            case ">":
            case "gt":
                return objectField.gt(DSL.val(value));
            case "<":
            case "lt":
                return objectField.lt(DSL.val(value));
            case ">=":
            case "gte":
                return objectField.ge(DSL.val(value));
            case "<=":
            case "lte":
                return objectField.le(DSL.val(value));
            case "!=":
            case "ne":
                return objectField.ne(DSL.val(value));
            case "like":
                return field.cast(String.class).like(value != null ? value.toString() : "");
            case "in":
                if (value instanceof List) {
                    return objectField.in((List<?>) value);
                } else if (value != null && value.getClass().isArray()) {
                    return objectField.in((Object[]) value);
                } else {
                    return objectField.in(value);
                }
            default:
                throw new IllegalArgumentException("Unsupported filter operator: " + filter.getOp());
        }
    }

    private void addGroupBy(SelectQuery<?> query, ReportDefinition reportDef, Table<?> table) {
        if (reportDef.getGroupBy() == null || reportDef.getGroupBy().isEmpty()) {
            return;
        }

        List<ReportableField> availableFields = reportableFieldRepository.findByEntity(reportDef.getEntity());
        Map<String, ReportableField> fieldMap = availableFields.stream()
            .collect(Collectors.toMap(ReportableField::getFieldName, f -> f));

        for (String groupField : reportDef.getGroupBy()) {
            ReportableField field = fieldMap.get(groupField);
            Field<?> fieldObj = DSL.field(DSL.name(field.getSourceColumn()));
            query.addGroupBy(fieldObj);
        }
    }

    private void addOrderBy(SelectQuery<?> query, ReportDefinition reportDef, Table<?> table) {
        if (reportDef.getSort() == null || reportDef.getSort().isEmpty()) {
            return;
        }

        List<ReportableField> availableFields = reportableFieldRepository.findByEntity(reportDef.getEntity());
        Map<String, ReportableField> fieldMap = availableFields.stream()
            .collect(Collectors.toMap(ReportableField::getFieldName, f -> f));

        for (ReportDefinition.Sort sort : reportDef.getSort()) {
            ReportableField field = fieldMap.get(sort.getField());
            Field<?> fieldObj = DSL.field(DSL.name(field.getSourceColumn()));

            if ("desc".equalsIgnoreCase(sort.getDir())) {
                query.addOrderBy(fieldObj.desc());
            } else {
                query.addOrderBy(fieldObj.asc());
            }
        }
    }
}