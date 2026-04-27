package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.repository.ProxyUserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserExpirationScheduler {

    private final ProxyUserRepository userRepository;

    public UserExpirationScheduler(ProxyUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredUsers() {
        userRepository.deleteByExpireAtBefore(LocalDateTime.now());
    }
}
