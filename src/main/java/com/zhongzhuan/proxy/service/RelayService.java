package com.zhongzhuan.proxy.service;

import com.zhongzhuan.proxy.model.Route;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RelayService {

    private final WebClient proxyWebClient;

    public RelayService(WebClient proxyWebClient) {
        this.proxyWebClient = proxyWebClient;
    }

    public ResponseEntity<byte[]> forwardRequest(
            Route route,
            String targetUrl,
            HttpMethod method,
            HttpHeaders headers,
            byte[] body) {

        WebClient.RequestBodySpec requestSpec = proxyWebClient
                .method(method)
                .uri(targetUrl);

        headers.forEach((name, values) -> {
            if (shouldForwardHeader(name)) {
                values.forEach(value -> requestSpec.header(name, value));
            }
        });

        WebClient.RequestHeadersSpec<?> spec;
        if (body != null && body.length > 0) {
            spec = requestSpec.bodyValue(body);
        } else {
            spec = requestSpec;
        }

        try {
            return spec
                    .exchangeToMono(response ->
                            response.bodyToMono(byte[].class)
                                    .defaultIfEmpty(new byte[0])
                                    .map(responseBody ->
                                            ResponseEntity
                                                    .status(response.statusCode().value())
                                                    .headers(response.headers().asHttpHeaders())
                                                    .body(responseBody))
                    )
                    .timeout(Duration.ofSeconds(60))
                    .onErrorResume(WebClientResponseException.class, e -> {
                        byte[] errBody = e.getResponseBodyAsByteArray();
                        return Mono.just(ResponseEntity.status(e.getStatusCode().value()).body(errBody));
                    })
                    .onErrorResume(WebClientRequestException.class, e -> {
                        byte[] errBody = ("{\"error\":\"请求目标服务失败: " + e.getMessage() + "\"}").getBytes();
                        return Mono.just(ResponseEntity.status(502).body(errBody));
                    })
                    .block();
        } catch (Exception e) {
            String errorMsg = "{\"error\":\"请求转发异常: " + e.getMessage() + "\"}";
            return ResponseEntity.status(502).body(errorMsg.getBytes());
        }
    }

    private boolean shouldForwardHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return !lower.equals("host")
                && !lower.equals("content-length")
                && !lower.equals("transfer-encoding")
                && !lower.equals("connection")
                && !lower.equals("x-api-key");
    }
}
