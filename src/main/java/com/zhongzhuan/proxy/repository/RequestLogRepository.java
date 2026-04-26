package com.zhongzhuan.proxy.repository;

import com.zhongzhuan.proxy.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long>,
        JpaSpecificationExecutor<RequestLog> {

    void deleteByCreatedAtBefore(LocalDateTime before);
}
