package com.vriddhi.exam;

import com.vriddhi.exam.config.ContentServiceProperties;
import com.vriddhi.exam.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, ContentServiceProperties.class})
@EnableScheduling
public class ExamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamServiceApplication.class, args);
    }
}
