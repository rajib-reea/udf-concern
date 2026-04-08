package com.udf.microservice.domain;

import java.util.List;
import java.util.Map;

public class ReportDefinition {

    private String entity;
    private List<String> columns;
    private List<Aggregation> aggregations;
    private List<Filter> filters;
    private List<String> groupBy;
    private List<Sort> sort;
    private Integer limit;

    // Constructors
    public ReportDefinition() {}

    public ReportDefinition(String entity, List<String> columns, List<Aggregation> aggregations,
                          List<Filter> filters, List<String> groupBy, List<Sort> sort, Integer limit) {
        this.entity = entity;
        this.columns = columns;
        this.aggregations = aggregations;
        this.filters = filters;
        this.groupBy = groupBy;
        this.sort = sort;
        this.limit = limit;
    }

    // Getters and Setters
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<Aggregation> getAggregations() { return aggregations; }
    public void setAggregations(List<Aggregation> aggregations) { this.aggregations = aggregations; }

    public List<Filter> getFilters() { return filters; }
    public void setFilters(List<Filter> filters) { this.filters = filters; }

    public List<String> getGroupBy() { return groupBy; }
    public void setGroupBy(List<String> groupBy) { this.groupBy = groupBy; }

    public List<Sort> getSort() { return sort; }
    public void setSort(List<Sort> sort) { this.sort = sort; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    // Inner classes
    public static class Aggregation {
        private String field;
        private String func;

        public Aggregation() {}

        public Aggregation(String field, String func) {
            this.field = field;
            this.func = func;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getFunc() { return func; }
        public void setFunc(String func) { this.func = func; }
    }

    public static class Filter {
        private String field;
        private String op;
        private Object value;

        public Filter() {}

        public Filter(String field, String op, Object value) {
            this.field = field;
            this.op = op;
            this.value = value;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getOp() { return op; }
        public void setOp(String op) { this.op = op; }

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class Sort {
        private String field;
        private String dir;

        public Sort() {}

        public Sort(String field, String dir) {
            this.field = field;
            this.dir = dir;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }
}