package com.udf.microservice.controller;

import com.udf.microservice.application.DynamicUiAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui")
public class DynamicUiController {

    @Autowired
    private DynamicUiAppService dynamicUiAppService;

    @GetMapping("/form-config/{entityType}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<DynamicUiAppService.DynamicFormConfig>> getFormConfig(
            @PathVariable String entityType) {
        try {
            DynamicUiAppService.DynamicFormConfig config = dynamicUiAppService.getFormConfig(entityType);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, config, "Form config retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/query-builder-config/{entityType}")
    public ResponseEntity<UdfDefinitionController.ApiResponse<DynamicUiAppService.QueryBuilderConfig>> getQueryBuilderConfig(
            @PathVariable String entityType) {
        try {
            DynamicUiAppService.QueryBuilderConfig config = dynamicUiAppService.getQueryBuilderConfig(entityType);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, config, "Query builder config retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/field-types")
    public ResponseEntity<UdfDefinitionController.ApiResponse<List<DynamicUiAppService.FieldTypeMetadata>>> getSupportedFieldTypes() {
        try {
            List<DynamicUiAppService.FieldTypeMetadata> fieldTypes = dynamicUiAppService.getSupportedFieldTypes();
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, fieldTypes, "Field types retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/validate-query")
    public ResponseEntity<UdfDefinitionController.ApiResponse<QueryValidationResult>> validateQuery(
            @RequestBody QueryValidationRequest request) {
        try {
            // Basic validation - in a real implementation, you'd validate against metadata
            boolean isValid = validateQueryStructure(request.getQuerySpec());
            String message = isValid ? "Query is valid" : "Query structure is invalid";

            QueryValidationResult result = new QueryValidationResult(isValid, message);
            return ResponseEntity.ok(new UdfDefinitionController.ApiResponse<>(true, result, "Query validation completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new UdfDefinitionController.ApiResponse<>(false, null, e.getMessage()));
        }
    }

    private boolean validateQueryStructure(Object querySpec) {
        // Basic structure validation - in a real implementation, this would be more comprehensive
        return querySpec != null;
    }

    // DTO Classes
    public static class QueryValidationRequest {
        private Object querySpec;

        public QueryValidationRequest() {}

        public QueryValidationRequest(Object querySpec) {
            this.querySpec = querySpec;
        }

        public Object getQuerySpec() { return querySpec; }
        public void setQuerySpec(Object querySpec) { this.querySpec = querySpec; }
    }

    public static class QueryValidationResult {
        private boolean valid;
        private String message;

        public QueryValidationResult() {}

        public QueryValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}