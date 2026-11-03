package com.vriddhi.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Every other service (auth, content, exam, gateway) registers itself here
 * on startup under its spring.application.name, and looks up the others by
 * that same name instead of a hardcoded host:port — see each service's
 * eureka.client config and the gateway's lb:// routes.
 *
 * Dashboard once running: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
