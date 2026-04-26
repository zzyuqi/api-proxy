package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.ProxyUser;
import com.zhongzhuan.proxy.model.TokenRecord;
import com.zhongzhuan.proxy.repository.ProxyUserRepository;
import com.zhongzhuan.proxy.repository.TokenRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProxyUser createUser(String username, String password, String role, Long initialTokens) {
        ProxyUser user = new ProxyUser();
        user.setUsername(username);
        user.setPassword(password); // plain text so admin can view/edit
        user.setRole(role != null ? role : "USER");
        user.setUserToken(generateToken());
        user.setTokenBalance(initialTokens != null ? initialTokens : 0L);
        user.setTokenUsed(0L);
        user.setEnabled(true);
        ProxyUser saved = userRepository.save(user);

        if (initialTokens != null && initialTokens > 0) {
            TokenRecord record = new TokenRecord();
            record.setUserId(saved.getId());
            record.setAmount(initialTokens);
            record.setType("ALLOCATE");
            record.setNote("初始分配");
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
    public ProxyUser updateUser(Long id, String username, String password, String role, Long tokenBalance, Boolean enabled) {
        ProxyUser user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        if (username != null) user.setUsername(username);
        if (password != null) user.setPassword(password);
        if (role != null) user.setRole(role);
        if (tokenBalance != null) {
            user.setTokenBalance(tokenBalance);
            // If balance increased, record the allocation
            // (simple approach — no record for direct edits)
        }
        if (enabled != null) user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Transactional
    public boolean deductTokens(Long userId, long amount, String note) {
        ProxyUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        long remaining = user.getTokenBalance() - user.getTokenUsed();
        if (remaining < amount) return false;

        user.setTokenUsed(user.getTokenUsed() + amount);
        userRepository.save(user);

        TokenRecord record = new TokenRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setType("USE");
        record.setNote(note);
        tokenRecordRepository.save(record);

        return true;
    }

    @Transactional
    public ProxyUser allocateTokens(Long userId, Long adminId, long amount, String note) {
        ProxyUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        user.setTokenBalance(user.getTokenBalance() + amount);
        ProxyUser saved = userRepository.save(user);

        TokenRecord record = new TokenRecord();
        record.setUserId(userId);
        record.setAdminId(adminId);
        record.setAmount(amount);
        record.setType(amount > 0 ? "ALLOCATE" : "DEDUCT");
        record.setNote(note);
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

    public long countUsers() {
        return userRepository.count();
    }

    private String generateToken() {
        return "usr_" + UUID.randomUUID().toString().replace("-", "");
    }
}
