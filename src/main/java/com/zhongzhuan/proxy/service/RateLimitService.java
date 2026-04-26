package com.zhongzhuan.proxy.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RateLimitService {

    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Check if a request should be allowed.
     * @param key unique key for the rate limit window (e.g., routeId:clientIp)
     * @param limit max requests per minute
     * @return true if allowed, false if rate limited
     */
    public boolean tryAcquire(String key, int limit) {
        if (limit <= 0) {
            return true; // unlimited
        }

        SlidingWindow window = windows.computeIfAbsent(key, k -> new SlidingWindow(limit));
        return window.tryAcquire();
    }

    private static class SlidingWindow {
        private final long windowSizeMs = 60_000L; // 1 minute
        private final int limit;

        // Ring buffer of timestamps
        private final long[] timestamps;
        private int index = 0;
        private boolean initialized = false;

        SlidingWindow(int limit) {
            this.limit = limit;
            this.timestamps = new long[limit];
        }

        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();

            if (!initialized) {
                timestamps[index] = now;
                index = (index + 1) % limit;
                initialized = true;
                return true;
            }

            long oldest = timestamps[index];
            if (now - oldest < windowSizeMs) {
                // Window is full, rate limited
                return false;
            }

            // Replace oldest timestamp with current
            timestamps[index] = now;
            index = (index + 1) % limit;
            return true;
        }
    }
}
