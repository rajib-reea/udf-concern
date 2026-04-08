package com.udf.microservice.controller;

import com.udf.microservice.application.UdfDefinitionAppService;
import com.udf.microservice.domain.UdfDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/udf-definitions")
public class UdfDefinitionController {

    @Autowired
    private UdfDefinitionAppService udfDefinitionAppService;

    @PostMapping
    public ResponseEntity<ApiResponse<UdfDefinition>> createDefinition(@RequestBody UdfDefinition definition) {
        try {
            UdfDefinition created = udfDefinitionAppService.createDefinition(definition);
            return ResponseEntity.ok(new ApiResponse<>(true, created, "UDF definition created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UdfDefinition>> updateDefinition(@PathVariable Long id,
                                                                       @RequestBody UdfDefinition definition) {
        try {
            UdfDefinition updated = udfDefinitionAppService.updateDefinition(id, definition);
            return ResponseEntity.ok(new ApiResponse<>(true, updated, "UDF definition updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDefinition(@PathVariable Long id) {
        try {
            udfDefinitionAppService.deleteDefinition(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "UDF definition deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UdfDefinition>>> getDefinitions(
            @RequestParam(required = false) String entityType) {
        try {
            List<UdfDefinition> definitions;
            if (entityType != null) {
                definitions = udfDefinitionAppService.getDefinitionsByEntityType(entityType);
            } else {
                // Return all definitions - you might want to add pagination
                definitions = List.of(); // Placeholder
            }
            return ResponseEntity.ok(new ApiResponse<>(true, definitions, "Definitions retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UdfDefinition>> getDefinition(@PathVariable Long id) {
        try {
            return udfDefinitionAppService.getDefinitionById(id)
                .map(definition -> ResponseEntity.ok(new ApiResponse<>(true, definition, "Definition found")))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    // DTO classes
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public ApiResponse() {}

        public ApiResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}