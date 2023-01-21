package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AggregatorFactory {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JsonConverter jsonConverter;

    @NonNull
    public <T> Mono<Aggregator<T>> create(@NonNull String aggregatorKey, @NonNull JsonNode parameters) {
        return Mono.just(aggregatorKey)
                    .map(key -> createAggregator(key, parameters));
    }

    private <T> Aggregator<T> createAggregator(final String aggregatorKey, final JsonNode parametersJson) {

        @SuppressWarnings("unchecked")
        final Aggregator<T> aggregator = this.applicationContext.getBean(aggregatorKey, Aggregator.class);
        final Object parameters = this.jsonConverter.convert(parametersJson, aggregator.getParametersClass());
        aggregator.setParameters(parameters);

        return aggregator;
    }
}
