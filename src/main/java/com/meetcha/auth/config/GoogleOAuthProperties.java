package com.meetcha.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
