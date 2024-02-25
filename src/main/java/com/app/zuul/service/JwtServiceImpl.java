package com.app.zuul.service;

import com.app.zuul.config.ServerConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtServiceImpl implements JwtService {

    // Autowired instance of ServerConfig for JWT configuration
    @Autowired
    ServerConfig serverConfig;

    // Extract the username from the JWT token
    public String extractUsername(String token){
        return extractClaim(token).getSubject();
    }

    // Extract the user roles from the JWT token
    public String extractRoles(String token){
        return String.valueOf(extractClaim(token).get("roles"));
    }

    // Retrieve the secret key used for signing JWT tokens
    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(serverConfig.getJwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Check if the JWT token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract the expiration date from the JWT token
    public Date extractExpiration(String token) {
        return extractClaim(token).getExpiration();
    }

    // Extract JWT claims from the token
    public Claims extractClaim(String token){
        try {
            return Jwts
                    .parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SecurityException |
                 IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    // Check if the JWT token is valid (not expired)
    public Boolean isValid(String token){
        try {
            extractClaim(token);
        } catch (Exception e){
            return true;
        }
        return isTokenExpired(token);
    }
}
