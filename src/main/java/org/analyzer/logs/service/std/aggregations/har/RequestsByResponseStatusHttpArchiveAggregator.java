package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.analyzer.logs.model.HttpArchiveBody.getFieldValueByPath;

@Component
public class RequestsByResponseStatusHttpArchiveAggregator implements HttpArchiveAggregator<Map<String, Integer>> {
    @NonNull
    @Override
    public String getName() {
        return "count-responses-by-statuses";
    }

    @Override
    @NonNull
    public Map<String, Integer> apply(@NonNull ArrayNode requests) {
        final Map<String, Integer> result = new HashMap<>();
        requests.forEach(request -> {
            final var status = getFieldValueByPath(request, "response", "status")
                                                .map(JsonNode::asInt)
                                                .orElse(0);
            final var statusText = getFieldValueByPath(request, "response", "statusText")
                                                    .map(JsonNode::asText)
                                                    .orElse("");
            result.compute(status + " " + statusText, (k, v) -> (v == null ? 0 : v) + 1);
        });

        return result;
    }
}