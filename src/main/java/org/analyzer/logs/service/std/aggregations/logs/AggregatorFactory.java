package org.analyzer.logs.service.std.aggregations.logs;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.LogsAggregator;
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
    public <T> LogsAggregator<T> create(@NonNull String aggregatorKey, @NonNull Object parameters) {
        return createAggregator(aggregatorKey, parameters);
    }

    @NonNull
    public <T> LogsAggregator<T> create(@NonNull String aggregatorKey, @NonNull JsonNode parameters) {
        return createAggregator(aggregatorKey, parameters);
    }

    private <T> LogsAggregator<T> createAggregator(final String aggregatorKey, final JsonNode parametersJson) {

        @SuppressWarnings("unchecked")
        final LogsAggregator<T> aggregator = this.applicationContext.getBean(aggregatorKey, LogsAggregator.class);
        final var parameters = this.jsonConverter.convert(parametersJson, aggregator.getParametersClass());
        aggregator.setParameters(parameters);

        return aggregator;
    }

    private <T> LogsAggregator<T> createAggregator(final String aggregatorKey, final Object parameters) {

        @SuppressWarnings("unchecked")
        final LogsAggregator<T> aggregator = this.applicationContext.getBean(aggregatorKey, LogsAggregator.class);
        aggregator.setParameters(parameters);

        return aggregator;
    }
}
