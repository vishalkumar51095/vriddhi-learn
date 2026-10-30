package com.vriddhi.exam.config;

import com.vriddhi.exam.security.InternalTokenProvider;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * @LoadBalanced makes Spring Cloud LoadBalancer resolve http://content-service
     * against Eureka instead of DNS.
     */
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
                // A fresh (cached, short-lived) signed JWT on every call — see InternalTokenProvider.
                .filter((request, next) -> next.exchange(
                        ClientRequest.from(request).headers(h -> h.setBearerAuth(tokens.token())).build()))
                .build();
    }
}
