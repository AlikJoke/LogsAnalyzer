package org.analyzer.config.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Value
@RequiredArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties("logs.analyzer.async-http.requests")
public class AsyncHttpRequestProperties {

    boolean keepAlive;
    int requestTimeout;
    int connectionTtl;
    int connectTimeout;
    boolean followRedirects;
    int maxRedirects;
    int ioThreads;
}
