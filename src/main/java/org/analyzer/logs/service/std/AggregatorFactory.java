package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AggregatorFactory {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private NoAggregationAggregator noAggregationAggregator;

    @NonNull
    public Mono<Aggregator> create(@NonNull Map<String, JsonNode> aggregator) {
        if (aggregator.size() > 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "More than one aggregator isn't allowed");
        }

        return Flux.fromIterable(aggregator.entrySet())
                    .map(this::createAggregator)
                    .single(this.noAggregationAggregator);
    }

    private Aggregator createAggregator(final Map.Entry<String, JsonNode> aggregatorEntry) {

        final Aggregator aggregator = this.applicationContext.getBean(aggregatorEntry.getKey(), Aggregator.class);
        final Object parameters = this.jsonConverter.convert(aggregatorEntry.getValue(), aggregator.getParametersClass());
        aggregator.setParameters(parameters);

        return aggregator;
    }
}
