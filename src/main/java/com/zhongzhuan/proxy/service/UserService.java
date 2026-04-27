package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.ProxyUser;
import com.zhongzhuan.proxy.model.TokenRecord;
import com.zhongzhuan.proxy.repository.ProxyUserRepository;
import com.zhongzhuan.proxy.repository.TokenRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final ProxyUserRepository userRepository;
    private final TokenRecordRepository tokenRecordRepository;

    public UserService(ProxyUserRepository userRepository,
                       TokenRecordRepository tokenRecordRepository) {
        this.userRepository = userRepository;
        this.tokenRecordRepository = tokenRecordRepository;
    }

    @Transactional
    public ProxyUser createUser(String username, String password, String role, Long initialRequests,
                               Integer concurrentLimit, Integer hourlyLimit) {
        return createUser(username, password, role, initialRequests, concurrentLimit, hourlyLimit, LocalDateTime.now().plusDays(30));
    }

    @Transactional
    public ProxyUser createUser(String username, String password, String role, Long initialRequests,
                               Integer concurrentLimit, Integer hourlyLimit, LocalDateTime expireAt) {
        ProxyUser user = new ProxyUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role != null ? role : "USER");
        user.setUserToken(generateToken());
        user.setRequestCount(initialRequests != null ? initialRequests : 0L);
        user.setRequestsUsed(0L);
        user.setConcurrentLimit(concurrentLimit != null ? concurrentLimit : 0);
        user.setHourlyLimit(hourlyLimit != null ? hourlyLimit : 0);
        user.setEnabled(true);
        user.setExpireAt(expireAt);
        ProxyUser saved = userRepository.save(user);

        if (initialRequests != null && initialRequests > 0) {
            TokenRecord record = new TokenRecord();
            record.setUserId(saved.getId());
            record.setAmount(initialRequests);
            record.setType("ALLOCATE");
            record.setNote("初始分配请求次数");
            tokenRecordRepository.save(record);
        }

        return saved;
    }

    public Optional<ProxyUser> authenticateByToken(String userToken) {
        return userRepository.findByUserTokenAndEnabledTrue(userToken);
    }

    /**
     * Authenticate user by username and plain text password.
     */
    public Optional<ProxyUser> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getEnabled() && password.equals(u.getPassword()));
    }

    @Transactional
    public ProxyUser updateUser(Long id, String username, String password, String role, Long requestCount,
                                Boolean enabled, Integer concurrentLimit, Integer hourlyLimit) {
        ProxyUser user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        if (username != null) user.setUsername(username);
        if (password != null) user.setPassword(password);
        if (role != null) user.setRole(role);
        if (requestCount != null) {
            user.setRequestCount(requestCount);
        }
        if (enabled != null) user.setEnabled(enabled);
        if (concurrentLimit != null) user.setConcurrentLimit(concurrentLimit);
        if (hourlyLimit != null) user.setHourlyLimit(hourlyLimit);
        return userRepository.save(user);
    }

    @Transactional
    public boolean incrementRequestUsage(Long userId, String note) {
        ProxyUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        long remaining = user.getRequestCount() - user.getRequestsUsed();
        if (remaining <= 0) return false;

        user.setRequestsUsed(user.getRequestsUsed() + 1);
        userRepository.save(user);

        TokenRecord record = new TokenRecord();
        record.setUserId(userId);
        record.setAmount(1L);
        record.setType("USE");
        record.setNote(note);
        tokenRecordRepository.save(record);

        return true;
    }

    @Transactional
    public ProxyUser allocateTokens(Long userId, Long adminId, long amount, String note) {
        ProxyUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        user.setRequestCount(user.getRequestCount() + amount);
        ProxyUser saved = userRepository.save(user);

        TokenRecord record = new TokenRecord();
        record.setUserId(userId);
        record.setAdminId(adminId);
        record.setAmount(amount);
        record.setType(amount > 0 ? "ALLOCATE" : "DEDUCT");
        record.setNote(note != null ? note : "分配请求次数");
        tokenRecordRepository.save(record);

        return saved;
    }

    public List<ProxyUser> listUsers() {
        return userRepository.findAll();
    }

    public ProxyUser getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public ProxyUser getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<TokenRecord> getUserTokenRecords(Long userId) {
        return tokenRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void updateLastActive(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActiveAt(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public long countUsers() {
        return userRepository.count();
    }

    private String generateToken() {
        return "usr_" + UUID.randomUUID().toString().replace("-", "");
    }
}
