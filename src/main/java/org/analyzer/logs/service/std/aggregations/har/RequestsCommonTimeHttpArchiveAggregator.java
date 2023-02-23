package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

import static org.analyzer.logs.model.HttpArchiveBody.getFieldValueByPath;

@Component
public class RequestsCommonTimeHttpArchiveAggregator implements HttpArchiveAggregator<Double> {
    @NonNull
    @Override
    public String getName() {
        return "common-time";
    }

    @Override
    @NonNull
    public Double apply(@NonNull ArrayNode requests) {
        return StreamSupport
                .stream(requests.spliterator(), false)
                .mapToDouble(request -> getFieldValueByPath(request, "time")
                                            .map(JsonNode::asDouble)
                                            .orElse(0.0)
                )
                .sum() / 1_000;
    }
}