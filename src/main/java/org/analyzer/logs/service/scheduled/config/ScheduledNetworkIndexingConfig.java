package org.analyzer.logs.service.scheduled.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(TelegramNotificationBotConfiguration.class)
public class ScheduledNetworkIndexingConfig {

    @Bean
    public WebClient webClientWithSsl() throws SSLException {
        final var sslContext = SslContextBuilder
                                        .forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build();
        final var client = HttpClient.create()
                                        .secure(t -> t.sslContext(sslContext))
                                        .resolver(DefaultAddressResolverGroup.INSTANCE);
        return WebClient
                .builder()
                    .clientConnector(new ReactorClientHttpConnector(client))
                .build();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
