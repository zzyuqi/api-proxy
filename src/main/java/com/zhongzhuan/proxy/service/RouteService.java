package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.Route;
import com.zhongzhuan.proxy.repository.RouteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final CopyOnWriteArrayList<Route> routeCache = new CopyOnWriteArrayList<>();

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        routeCache.clear();
        routeCache.addAll(routeRepository.findByStatus("ENABLED"));
    }

    public Route matchRoute(String path, String method) {
        // First pass: exact match (no wildcard) takes priority
        for (Route route : routeCache) {
            if (route.getPathPattern() != null && !route.getPathPattern().contains("**")) {
                if (pathMatcher.match(route.getPathPattern(), path)) {
                    if (route.getMethod() == null || route.getMethod().isEmpty()
                            || route.getMethod().equalsIgnoreCase(method)) {
                        return route;
                    }
                }
            }
        }
        // Second pass: wildcard match
        for (Route route : routeCache) {
            if (route.getPathPattern() != null && route.getPathPattern().contains("**")) {
                if (pathMatcher.match(route.getPathPattern(), path)) {
                    if (route.getMethod() == null || route.getMethod().isEmpty()
                            || route.getMethod().equalsIgnoreCase(method)) {
                        return route;
                    }
                }
            }
        }
        return null;
    }

    public String buildTargetUrl(Route route, String path) {
        String targetUrl = route.getTargetUrl();
        if (targetUrl.endsWith("/")) {
            targetUrl = targetUrl.substring(0, targetUrl.length() - 1);
        }

        String pattern = route.getPathPattern();
        // If pattern has no wildcard (**), use targetUrl as-is (exact match)
        if (!pattern.contains("**")) {
            return targetUrl;
        }

        // Extract wildcard path from the pattern
        // e.g., pattern = /api/proxy/gpt/** , path = /api/proxy/gpt/v1/chat
        // We extract the part matching ** and append to targetUrl
        String wildcardPath = pathMatcher.extractPathWithinPattern(pattern, path);
        if (!wildcardPath.isEmpty()) {
            targetUrl += "/" + wildcardPath;
        }

        return targetUrl;
    }

    public Route saveRoute(Route route) {
        Route saved = routeRepository.save(route);
        refreshCache();
        return saved;
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
        refreshCache();
    }

    public List<Route> listRoutes() {
        return routeRepository.findAll();
    }

    public Route getRoute(Long id) {
        return routeRepository.findById(id).orElse(null);
    }
}
