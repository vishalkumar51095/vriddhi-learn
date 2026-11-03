package com.vriddhi.progress.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "content-service")
public record ContentServiceProperties(String baseUrl) {
}
