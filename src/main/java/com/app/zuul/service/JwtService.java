package com.app.zuul.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String extractUsername(String token);
    String extractRoles(String token);
    Claims extractClaim(String token);

    Boolean isValid(String token);
}
