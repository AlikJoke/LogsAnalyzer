package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestsByMethodsHttpArchiveAggregator implements HttpArchiveAggregator<Map<String, Integer>> {
    @NonNull
    @Override
    public String getName() {
        return "count-requests-by-methods";
    }

    @Override
    @NonNull
    public Map<String, Integer> apply(@NonNull ArrayNode requests) {
        final Map<String, Integer> result = new HashMap<>();
        requests.forEach(request -> {
            final var requestMethod = HttpArchiveBody.getFieldValueByPath(request, "request", "method")
                                                        .map(JsonNode::asText)
                                                        .orElse(null);
            result.compute(requestMethod, (k, v) -> (v == null ? 0 : v) + 1);
        });

        return result;
    }
}