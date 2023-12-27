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

    @Autowired
    ServerConfig serverConfig;

    public String extractUsername(String token){
        return extractClaim(token).getSubject();
    }

    public String extractRoles(String token){
        return String.valueOf(extractClaim(token).get("roles"));
    }
    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(serverConfig.getJwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token).getExpiration();
    }

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
    public Boolean isValid(String token){
        try {
            extractClaim(token);
        } catch (Exception e){
            return true;
        }
        return isTokenExpired(token);
    }
}
