package com.app.zuul.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    // List of open API endpoints that do not require security validation
    public static final List<String> openApiEndpoints = List.of("/auth");

    // Predicate to check if the request URI is secured or open
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    // Check if the request URI contains any open API endpoint
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
