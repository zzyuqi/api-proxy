package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.RequestLog;
import com.zhongzhuan.proxy.repository.RequestLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@EnableAsync
public class LogService {

    private final RequestLogRepository requestLogRepository;

    @Value("${proxy.log.retention-days:30}")
    private int retentionDays;

    public LogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Async
    public void saveLog(RequestLog log) {
        requestLogRepository.save(log);
    }

    /**
     * Clean up old logs daily (runs at 2:00 AM).
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        requestLogRepository.deleteByCreatedAtBefore(cutoff);
    }
}
