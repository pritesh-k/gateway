package com.app.zuul.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("config-server-mongo")
public class ServerConfig {

    @Value("${jwtSecret}")
    private  String jwtSecret;

    public String getJwtSecret() {
        return jwtSecret;
    }
}

