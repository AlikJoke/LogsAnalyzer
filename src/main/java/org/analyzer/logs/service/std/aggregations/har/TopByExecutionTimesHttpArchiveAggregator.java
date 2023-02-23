package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.analyzer.logs.model.HttpArchiveBody.getFieldValueByPath;

abstract class TopByExecutionTimesHttpArchiveAggregator implements HttpArchiveAggregator<Map<String, Double>> {

    @Override
    @NonNull
    public Map<String, Double> apply(@NonNull ArrayNode requests) {
        return StreamSupport.stream(requests.spliterator(), false)
                                .map(request ->
                                        getFieldValueByPath(request, getTimingPath())
                                                        .map(JsonNode::asDouble)
                                                        .map(timing ->
                                                                ImmutablePair.of(
                                                                        getFieldValueByPath(request, "request", "url")
                                                                                    .map(JsonNode::asText)
                                                                                    .orElseThrow(),
                                                                        timing)
                                                        )
                                                        .orElse(null)
                                )
                                .filter(Objects::nonNull)
                                .sorted((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()))
                                .limit(5)
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @NonNull
    protected abstract String[] getTimingPath();
}