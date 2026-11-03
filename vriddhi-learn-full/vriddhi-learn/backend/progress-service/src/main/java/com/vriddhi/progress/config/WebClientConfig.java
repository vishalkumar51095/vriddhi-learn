package com.vriddhi.progress.config;

import com.vriddhi.progress.security.InternalTokenProvider;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /** @LoadBalanced makes Spring Cloud LoadBalancer resolve http://content-service against Eureka. */
    @LoadBalanced
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient contentServiceWebClient(WebClient.Builder loadBalancedWebClientBuilder,
                                              ContentServiceProperties properties,
                                              InternalTokenProvider tokens) {
        return loadBalancedWebClientBuilder
                .baseUrl(properties.baseUrl())
                // A fresh (cached, short-lived) signed JWT on every call, carrying the PROGRESS_SERVICE role.
                .filter((request, next) -> next.exchange(
                        ClientRequest.from(request).headers(h -> h.setBearerAuth(tokens.token())).build()))
                .build();
    }
}
