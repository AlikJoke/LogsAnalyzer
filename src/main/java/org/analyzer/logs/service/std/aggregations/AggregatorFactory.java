package org.analyzer.logs.service.std.aggregations;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AggregatorFactory {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JsonConverter jsonConverter;

    @NonNull
    public <T> Aggregator<T> create(@NonNull String aggregatorKey, @NonNull Object parameters) {
        return createAggregator(aggregatorKey, parameters);
    }

    @NonNull
    public <T> Aggregator<T> create(@NonNull String aggregatorKey, @NonNull JsonNode parameters) {
        return createAggregator(aggregatorKey, parameters);
    }

    private <T> Aggregator<T> createAggregator(final String aggregatorKey, final JsonNode parametersJson) {

        @SuppressWarnings("unchecked")
        final Aggregator<T> aggregator = this.applicationContext.getBean(aggregatorKey, Aggregator.class);
        final var parameters = this.jsonConverter.convert(parametersJson, aggregator.getParametersClass());
        aggregator.setParameters(parameters);

        return aggregator;
    }

    private <T> Aggregator<T> createAggregator(final String aggregatorKey, final Object parameters) {

        @SuppressWarnings("unchecked")
        final Aggregator<T> aggregator = this.applicationContext.getBean(aggregatorKey, Aggregator.class);
        aggregator.setParameters(parameters);

        return aggregator;
    }
}
