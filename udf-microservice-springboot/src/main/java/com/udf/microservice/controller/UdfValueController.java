package com.udf.microservice.controller;

import com.udf.microservice.application.UdfValueAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/udf-values")
public class UdfValueController {

    @Autowired
    private UdfValueAppService udfValueAppService;

    @PostMapping
    public ResponseEntity<UdfDefinitionController.ApiResponse<Void>> setUdfValue(
            @RequestBody SetUdfValueRequest request) {
        try {
            udfValueAppService.setUdfValue(request.getEntityType(), request.getEntityId(),
                                          request.getFieldId(), request.getValue());
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, null, "UDF value set successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<UdfDefinitionController.ApiResponse<Void>> setMultipleUdfValues(
            @RequestBody SetMultipleUdfValuesRequest request) {
        try {
            udfValueAppService.setMultipleUdfValues(request.getEntityType(), request.getEntityId(),
                                                   request.getFieldValues());
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, null, "UDF values set successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<Map<String, Object>>> getUdfValues(
            @PathVariable String entityType, @PathVariable Long entityId) {
        try {
            Map<String, Object> values = udfValueAppService.getUdfValues(entityType, entityId);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, values, "UDF values retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/{entityType}/{entityId}/{fieldName}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<Object>> getUdfValue(
            @PathVariable String entityType, @PathVariable Long entityId, @PathVariable String fieldName) {
        try {
            Object value = udfValueAppService.getUdfValue(entityType, entityId, fieldName);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, value, "UDF value retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @DeleteMapping("/{entityType}/{entityId}/{fieldId}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<Void>> deleteUdfValue(
            @PathVariable String entityType, @PathVariable Long entityId, @PathVariable Long fieldId) {
        try {
            udfValueAppService.deleteUdfValue(entityType, entityId, fieldId);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, null, "UDF value deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // DTO classes
    public static class SetUdfValueRequest {
        private String entityType;
        private Long entityId;
        private Long fieldId;
        private Object value;

        public SetUdfValueRequest() {}

        public SetUdfValueRequest(String entityType, Long entityId, Long fieldId, Object value) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.fieldId = fieldId;
            this.value = value;
        }

        // Getters and setters
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }

        public Long getFieldId() { return fieldId; }
        public void setFieldId(Long fieldId) { this.fieldId = fieldId; }

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class SetMultipleUdfValuesRequest {
        private String entityType;
        private Long entityId;
        private Map<Long, Object> fieldValues;

        public SetMultipleUdfValuesRequest() {}

        public SetMultipleUdfValuesRequest(String entityType, Long entityId, Map<Long, Object> fieldValues) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.fieldValues = fieldValues;
        }

        // Getters and setters
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }

        public Map<Long, Object> getFieldValues() { return fieldValues; }
        public void setFieldValues(Map<Long, Object> fieldValues) { this.fieldValues = fieldValues; }
    }
}