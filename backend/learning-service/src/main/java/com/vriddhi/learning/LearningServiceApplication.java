package com.vriddhi.learning;

import com.vriddhi.learning.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LearningServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }
}
