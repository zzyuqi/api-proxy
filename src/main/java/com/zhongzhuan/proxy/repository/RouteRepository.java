package com.zhongzhuan.proxy.repository;

import com.zhongzhuan.proxy.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByStatus(String status);
}
