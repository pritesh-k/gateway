package com.app.zuul.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("config-server-mongo")
public class ServerConfig {

    // Value for JWT Secret retrieved from the configuration properties
    @Value("${jwtSecret}")
    private String jwtSecret;

    // Getter method to retrieve the JWT Secret
    public String getJwtSecret() {
        return jwtSecret;
    }
}
