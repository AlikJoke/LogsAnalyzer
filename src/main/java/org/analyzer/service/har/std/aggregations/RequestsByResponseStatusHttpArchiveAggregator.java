package org.analyzer.service.har.std.aggregations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.service.har.HttpArchiveAggregator;
import org.analyzer.service.har.HttpArchiveBody;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
            final var status = HttpArchiveBody.getFieldValueByPath(request, "response", "status")
                                                .map(JsonNode::asInt)
                                                .orElse(0);
            final var statusText = HttpArchiveBody.getFieldValueByPath(request, "response", "statusText")
                                                    .map(JsonNode::asText)
                                                    .orElse("");
            result.compute(status + " " + statusText, (k, v) -> (v == null ? 0 : v) + 1);
        });

        return result;
    }
}