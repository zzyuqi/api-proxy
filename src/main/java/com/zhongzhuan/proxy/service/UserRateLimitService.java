package com.zhongzhuan.proxy.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户频率限制服务
 * 跟踪用户的并发请求数和每小时请求次数
 */
@Service
public class UserRateLimitService {

    // 用户当前并发数
    private final ConcurrentHashMap<Long, AtomicInteger> concurrentCounts = new ConcurrentHashMap<>();

    // 用户每小时请求记录 (userId -> (hourKey -> count))
    private final ConcurrentHashMap<Long, HourlyCounter> hourlyCounts = new ConcurrentHashMap<>();

    /**
     * 尝试获取并发许可
     * @param userId 用户ID
     * @param limit 限制数量，0表示不限制
     * @return true 表示允许，false 表示被限制
     */
    public boolean tryAcquireConcurrent(Long userId, int limit) {
        if (limit <= 0) {
            return true; // 不限制
        }

        AtomicInteger count = concurrentCounts.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int current = count.incrementAndGet();

        if (current > limit) {
            count.decrementAndGet();
            return false;
        }
        return true;
    }

    /**
     * 释放并发许可
     */
    public void releaseConcurrent(Long userId) {
        AtomicInteger count = concurrentCounts.get(userId);
        if (count != null && count.decrementAndGet() <= 0) {
            concurrentCounts.remove(userId);
        }
    }

    /**
     * 检查并增加小时请求数
     * @param userId 用户ID
     * @param limit 限制数量，0表示不限制
     * @return true 表示允许，false 表示被限制
     */
    public boolean tryAcquireHourly(Long userId, int limit) {
        if (limit <= 0) {
            return true; // 不限制
        }

        HourlyCounter counter = hourlyCounts.computeIfAbsent(userId, k -> new HourlyCounter());
        return counter.tryIncrement(limit);
    }

    /**
     * 清理过期的小时记录
     */
    public void cleanupExpired() {
        hourlyCounts.values().forEach(HourlyCounter::cleanupExpired);
    }

    /**
     * 获取当前并发数
     */
    public int getCurrentConcurrent(Long userId) {
        AtomicInteger count = concurrentCounts.get(userId);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取当前小时请求数
     */
    public int getCurrentHourly(Long userId) {
        HourlyCounter counter = hourlyCounts.get(userId);
        return counter != null ? counter.getCurrentCount() : 0;
    }

    /**
     * 小时计数器
     */
    private static class HourlyCounter {
        private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
        private volatile long lastCleanup = System.currentTimeMillis();

        boolean tryIncrement(int limit) {
            String hourKey = getCurrentHourKey();
            AtomicInteger count = counts.computeIfAbsent(hourKey, k -> new AtomicInteger(0));
            int current = count.incrementAndGet();

            if (current > limit) {
                count.decrementAndGet();
                return false;
            }

            cleanupExpired();
            return true;
        }

        int getCurrentCount() {
            String hourKey = getCurrentHourKey();
            AtomicInteger count = counts.get(hourKey);
            return count != null ? count.get() : 0;
        }

        void cleanupExpired() {
            long now = System.currentTimeMillis();
            if (now - lastCleanup < 60_000) {
                return; // 每分钟最多清理一次
            }
            lastCleanup = now;

            String currentKey = getCurrentHourKey();
            counts.keySet().removeIf(key -> !key.equals(currentKey));
        }

        private String getCurrentHourKey() {
            return String.valueOf(System.currentTimeMillis() / 3600000);
        }
    }
}
