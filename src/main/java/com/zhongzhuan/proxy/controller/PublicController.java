package com.zhongzhuan.proxy.controller;

import com.zhongzhuan.proxy.dto.RouteResponse;
import com.zhongzhuan.proxy.model.Route;
import com.zhongzhuan.proxy.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final RouteService routeService;

    public PublicController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> getPublicRoutes() {
        List<Route> routes = routeService.listRoutes().stream()
                .filter(r -> "ENABLED".equals(r.getStatus()))
                .filter(r -> r.getUserVisible() == null || r.getUserVisible())
                .toList();

        List<RouteResponse> responses = routes.stream()
                .map(RouteResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}