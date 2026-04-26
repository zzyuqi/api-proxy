package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.dto.ModelConfigRequest;
import com.zhongzhuan.proxy.model.ModelConfig;
import com.zhongzhuan.proxy.repository.ModelConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelConfigService {

    private final ModelConfigRepository modelConfigRepository;

    public ModelConfigService(ModelConfigRepository modelConfigRepository) {
        this.modelConfigRepository = modelConfigRepository;
    }

    public ModelConfig createModel(ModelConfigRequest request) {
        ModelConfig config = new ModelConfig();
        config.setName(request.getName());
        config.setProvider(request.getProvider());
        config.setApiUrl(request.getApiUrl());
        config.setApiKey(request.getApiKey());
        config.setDefaultModelId(request.getDefaultModelId());
        config.setModels(request.getModels());
        config.setStatus(request.getStatus() != null ? request.getStatus() : "ENABLED");
        config.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        return modelConfigRepository.save(config);
    }

    public Optional<ModelConfig> updateModel(Long id, ModelConfigRequest request) {
        Optional<ModelConfig> opt = modelConfigRepository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        ModelConfig config = opt.get();
        config.setName(request.getName());
        config.setProvider(request.getProvider());
        config.setApiUrl(request.getApiUrl());
        config.setApiKey(request.getApiKey());
        config.setDefaultModelId(request.getDefaultModelId());
        config.setModels(request.getModels());
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        if (request.getDisplayOrder() != null) {
            config.setDisplayOrder(request.getDisplayOrder());
        }
        return Optional.of(modelConfigRepository.save(config));
    }

    public boolean deleteModel(Long id) {
        if (!modelConfigRepository.existsById(id)) {
            return false;
        }
        modelConfigRepository.deleteById(id);
        return true;
    }

    public List<ModelConfig> listModels() {
        return modelConfigRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<ModelConfig> getModel(Long id) {
        return modelConfigRepository.findById(id);
    }

    public List<ModelConfig> getEnabledModels() {
        return modelConfigRepository.findByStatusOrderByDisplayOrderAsc("ENABLED");
    }
}