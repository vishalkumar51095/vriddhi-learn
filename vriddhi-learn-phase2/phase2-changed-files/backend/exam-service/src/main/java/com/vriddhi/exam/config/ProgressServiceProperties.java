package com.vriddhi.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "progress-service")
public record ProgressServiceProperties(String baseUrl) {
}
