package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.ApiKey;
import com.zhongzhuan.proxy.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKey createKey(String name, Long userId) {
        ApiKey apiKey = new ApiKey();
        apiKey.setName(name);
        apiKey.setKeyValue(generateKey());
        apiKey.setEnabled(true);
        apiKey.setUserId(userId);
        return apiKeyRepository.save(apiKey);
    }

    public Optional<ApiKey> updateKey(Long id, String name, Boolean enabled) {
        Optional<ApiKey> opt = apiKeyRepository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        ApiKey apiKey = opt.get();
        if (name != null && !name.isBlank()) {
            apiKey.setName(name);
        }
        if (enabled != null) {
            apiKey.setEnabled(enabled);
        }
        return Optional.of(apiKeyRepository.save(apiKey));
    }

    public Optional<ApiKey> validateKey(String keyValue) {
        return apiKeyRepository.findByKeyValueAndEnabledTrue(keyValue);
    }

    public List<ApiKey> listKeys() {
        return apiKeyRepository.findAll();
    }

    public Optional<ApiKey> getKey(Long id) {
        return apiKeyRepository.findById(id);
    }

    public void deleteKey(Long id) {
        apiKeyRepository.deleteById(id);
    }

    private String generateKey() {
        return "pk_" + UUID.randomUUID().toString().replace("-", "");
    }
}
