package com.app.zuul.config;

import com.app.zuul.filters.AuthenticationFilter;
import com.app.zuul.filters.ProductFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Autowired
    AuthenticationFilter filter;

    @Autowired
    ProductFilter productFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()
                // Route for USER-SERVICE
                .route("USER-SERVICE", r -> r.path("/auth/**", "/users/**")
                        .filters(f -> f.filter(productFilter.apply(new ProductFilter())))
                        .uri("lb://user-service")
                )
                // Route for PRODUCT-SERVICE
                .route("PRODUCT-SERVICE", r -> r.path("/products/**")
                        .filters(f -> f.filter(productFilter.apply(new ProductFilter())))
                        .uri("lb://product-service"))

                // Route for user-service API documentation
                .route(r -> r.path("/user-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://user-service"))

                // Route for product-service API documentation
                .route(r -> r.path("/product-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://product-service"))

                // Route for user registration
                .route(r -> r.path("/auth/api/v1/register").and().method(HttpMethod.POST).uri("lb://user-service"))

                // Route for user login
                .route(r -> r.path("/auth/api/v1/login").and().method(HttpMethod.POST).uri("lb://user-service"))

                // Route for deleting a product by ID
                .route(r -> r.path("/api/v1/products/{id}").and().method(HttpMethod.DELETE).uri("lb://product-service"))
                .build();
    }
}
