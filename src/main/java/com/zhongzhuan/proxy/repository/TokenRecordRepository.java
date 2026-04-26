package com.zhongzhuan.proxy.repository;

import com.zhongzhuan.proxy.model.TokenRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRecordRepository extends JpaRepository<TokenRecord, Long> {

    List<TokenRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<TokenRecord> findByUserId(Long userId, Pageable pageable);
}
