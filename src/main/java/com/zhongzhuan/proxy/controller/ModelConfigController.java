package com.zhongzhuan.proxy.controller;

import com.zhongzhuan.proxy.dto.ModelConfigRequest;
import com.zhongzhuan.proxy.dto.ModelConfigResponse;
import com.zhongzhuan.proxy.model.ModelConfig;
import com.zhongzhuan.proxy.service.ModelConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/models")
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    public ModelConfigController(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    @PostMapping
    public ResponseEntity<ModelConfigResponse> createModel(@Valid @RequestBody ModelConfigRequest request) {
        ModelConfig saved = modelConfigService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ModelConfigResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<ModelConfigResponse>> listModels() {
        List<ModelConfig> configs = modelConfigService.listModels();
        List<ModelConfigResponse> responses = configs.stream()
                .map(ModelConfigResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModelConfigResponse> getModel(@PathVariable Long id) {
        return modelConfigService.getModel(id)
                .map(ModelConfigResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModelConfigResponse> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelConfigRequest request) {
        return modelConfigService.updateModel(id, request)
                .map(ModelConfigResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        if (modelConfigService.deleteModel(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<ModelConfigResponse>> getEnabledModels() {
        List<ModelConfig> configs = modelConfigService.getEnabledModels();
        List<ModelConfigResponse> responses = configs.stream()
                .map(ModelConfigResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}