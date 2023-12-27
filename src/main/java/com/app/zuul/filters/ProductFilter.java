package com.app.zuul.filters;

import com.app.zuul.config.RouterValidator;
import com.app.zuul.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ProductFilter extends AbstractGatewayFilterFactory<ProductFilter> {

    @Autowired
    private RouterValidator routerValidator;

    @Autowired
    JwtService jwtService;

    @Override
    public GatewayFilter apply(ProductFilter config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String uri = request.getURI().getPath();
            if (routerValidator.isSecured.test(request)) {
                if (this.isAuthMissing(request)) {
                    return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                final String token = this.getAuthHeader(request).substring(7);

                if (jwtService.isValid(token)) {
                    return this.onError(exchange, HttpStatus.FORBIDDEN);
                }
                if (!userHasRequiredRole(exchange, token)){
                    // User doesn't have the required role, return unauthorized status
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                this.updateRequest(exchange, token);

            }
            return chain.filter(exchange);
        });
    }

    private boolean userHasRequiredRole(ServerWebExchange exchange, String token) {
        // Example: Extract user roles from the JWT token

        String userRoles = jwtService.extractRoles(token);

        // Implement your logic to check if the user has the required role
        return userRoles.equalsIgnoreCase("USER") || userRoles.equalsIgnoreCase("ADMIN");
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private void updateRequest(ServerWebExchange exchange, String token) {
        Claims claims = jwtService.extractClaim(token);
        exchange.getRequest().mutate()
                .header("email", String.valueOf(claims.get("email")))
                .header("userId", String.valueOf(claims.get("userId")))
                .build();
    }
}
