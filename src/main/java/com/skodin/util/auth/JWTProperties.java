package com.skodin.util.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.security.jwt")
public class JWTProperties {
    private String secret;
    private long access;
}
