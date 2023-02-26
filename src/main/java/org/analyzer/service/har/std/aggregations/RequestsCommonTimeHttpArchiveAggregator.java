package org.analyzer.service.har.std.aggregations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.service.har.HttpArchiveAggregator;
import org.analyzer.service.har.HttpArchiveBody;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

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
                .mapToDouble(request -> HttpArchiveBody.getFieldValueByPath(request, "time")
                                            .map(JsonNode::asDouble)
                                            .orElse(0.0)
                )
                .sum() / 1_000;
    }
}