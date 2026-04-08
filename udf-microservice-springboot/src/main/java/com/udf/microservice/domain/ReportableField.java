package com.udf.microservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "reportable_fields")
public class ReportableField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "source_column", nullable = false)
    private String sourceColumn;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "is_aggregatable", nullable = false)
    private boolean aggregatable = false;

    @Column(name = "is_filterable", nullable = false)
    private boolean filterable = true;

    @Column(nullable = false)
    private String entity;

    // Constructors
    public ReportableField() {}

    public ReportableField(String fieldName, String sourceColumn, String dataType,
                          boolean aggregatable, boolean filterable, String entity) {
        this.fieldName = fieldName;
        this.sourceColumn = sourceColumn;
        this.dataType = dataType;
        this.aggregatable = aggregatable;
        this.filterable = filterable;
        this.entity = entity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getSourceColumn() { return sourceColumn; }
    public void setSourceColumn(String sourceColumn) { this.sourceColumn = sourceColumn; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public boolean isAggregatable() { return aggregatable; }
    public void setAggregatable(boolean aggregatable) { this.aggregatable = aggregatable; }

    public boolean isFilterable() { return filterable; }
    public void setFilterable(boolean filterable) { this.filterable = filterable; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
}