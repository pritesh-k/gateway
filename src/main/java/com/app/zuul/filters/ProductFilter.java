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

    // Autowired instance of RouterValidator for URL validation
    @Autowired
    private RouterValidator routerValidator;

    // Autowired instance of JwtService for JWT related operations
    @Autowired
    JwtService jwtService;

    // Apply method to define the behavior of the ProductFilter
    @Override
    public GatewayFilter apply(ProductFilter config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String uri = request.getURI().getPath();

            // Check if the URL requires security
            if (routerValidator.isSecured.test(request)) {
                // Check if Authorization header is missing
                if (this.isAuthMissing(request)) {
                    return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                // Retrieve the JWT token from the Authorization header and remove the "Bearer " prefix
                final String token = this.getAuthHeader(request).substring(7);

                // Validate the JWT token
                if (jwtService.isValid(token)) {
                    return this.onError(exchange, HttpStatus.FORBIDDEN);
                }

                // Check if the user has the required role
                if (!userHasRequiredRole(exchange, token)) {
                    // User doesn't have the required role, return unauthorized status
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Update the request headers with user information from JWT claims
                this.updateRequest(exchange, token);
            }

            // Continue with the filter chain
            return chain.filter(exchange);
        });
    }

    // Check if the user has the required role
    private boolean userHasRequiredRole(ServerWebExchange exchange, String token) {
        // Example: Extract user roles from the JWT token
        String userRoles = jwtService.extractRoles(token);

        // Implement your logic to check if the user has the required role
        return userRoles.equalsIgnoreCase("USER") || userRoles.equalsIgnoreCase("ADMIN");
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
