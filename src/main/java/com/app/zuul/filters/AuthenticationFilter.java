package com.app.zuul.filters;

import com.app.zuul.config.RouterValidator;
import com.app.zuul.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GatewayFilter {

    // Autowired instance of JwtService for JWT related operations
    @Autowired
    JwtService jwtService;

    // Autowired instance of RouterValidator for URL validation
    @Autowired
    private RouterValidator routerValidator;

    // Implementation of the filter method for authentication
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if the URL requires security
        if (routerValidator.isSecured.test(request)) {
            // Check if Authorization header is missing
            if (this.isAuthMissing(request)) {
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Retrieve the JWT token from the Authorization header
            final String token = this.getAuthHeader(request);

            // Validate the JWT token
            if (jwtService.isValid(token)) {
                return this.onError(exchange, HttpStatus.FORBIDDEN);
            }

            // Update the request headers with user information from JWT claims
            this.updateRequest(exchange, token);
        }

        // Continue with the filter chain
        return chain.filter(exchange);
    }

    // Handle error response with specified HTTP status
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    // Retrieve the Authorization header from the request
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    // Check if Authorization header is missing
    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    // Update the request headers with user information from JWT claims
    private void updateRequest(ServerWebExchange exchange, String token) {
        Claims claims = jwtService.extractClaim(token);
        exchange.getRequest().mutate()
                .header("email", String.valueOf(claims.get("email")))
                .header("userId", String.valueOf(claims.get("userId")))
                .build();
    }
}
