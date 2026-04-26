package com.zhongzhuan.proxy.filter;

import com.zhongzhuan.proxy.service.RateLimitService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Fallback rate limit filter for requests that don't match any route.
 * Route-specific rate limiting is handled in RelayService.
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Default global rate limit check (100 req/s per IP for unmatched paths)
        String path = request.getRequestURI();
        if (path.startsWith("/api/proxy/")) {
            String clientIp = getClientIp(request);
            if (!rateLimitService.tryAcquire("global:" + clientIp, 200)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"请求过于频繁，请稍后再试\"}");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
