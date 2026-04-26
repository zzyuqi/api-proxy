package com.zhongzhuan.proxy.repository;

import com.zhongzhuan.proxy.model.ProxyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProxyUserRepository extends JpaRepository<ProxyUser, Long> {

    Optional<ProxyUser> findByUsername(String username);

    Optional<ProxyUser> findByUserTokenAndEnabledTrue(String userToken);

    long countByRole(String role);
}
