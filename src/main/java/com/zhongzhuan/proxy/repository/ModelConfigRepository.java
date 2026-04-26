package com.zhongzhuan.proxy.repository;

import com.zhongzhuan.proxy.model.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long> {
    List<ModelConfig> findByStatusOrderByDisplayOrderAsc(String status);
    List<ModelConfig> findAllByOrderByDisplayOrderAsc();
}