package com.conexa.starwars.common.swapi;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Creates the HTTP clients used to consume SWAPI.
 * <p>
 * SWAPI endpoints are declared as {@code @HttpExchange} interfaces whose proxies are built
 * explicitly from a dedicated {@link RestClient}, rather than via {@code @ImportHttpServices},
 * so the underlying client stays visible and can be bound to {@code MockRestServiceServer} in tests.
 * Timeouts come from the standard {@code spring.http.clients.*} properties.
 */
@Configuration
@EnableConfigurationProperties(SwapiProperties.class)
public class SwapiClientConfig {

    @Bean
    RestClient swapiRestClient(RestClient.Builder builder, SwapiProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }

    @Bean
    HttpServiceProxyFactory swapiProxyFactory(RestClient swapiRestClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(swapiRestClient))
                .build();
    }

    @Bean
    SwapiPeopleClient swapiPeopleClient(HttpServiceProxyFactory swapiProxyFactory) {
        return swapiProxyFactory.createClient(SwapiPeopleClient.class);
    }
}
