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
public class RequestsByUrlsHttpArchiveAggregator implements HttpArchiveAggregator<Map<String, Integer>> {
    @NonNull
    @Override
    public String getName() {
        return "count-requests-by-urls";
    }

    @Override
    @NonNull
    public Map<String, Integer> apply(@NonNull ArrayNode requests) {
        final Map<String, Integer> result = new HashMap<>();
        requests.forEach(request -> {
            final var url = HttpArchiveBody.getFieldValueByPath(request, "request", "url")
                                                .map(JsonNode::asText)
                                                .orElse(null);
            result.compute(url, (k, v) -> (v == null ? 0 : v) + 1);
        });

        return result;
    }
}