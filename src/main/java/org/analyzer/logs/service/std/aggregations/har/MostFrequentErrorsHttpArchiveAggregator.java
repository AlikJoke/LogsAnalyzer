package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.analyzer.logs.model.HttpArchiveBody.getFieldValueByPath;

@Component
public class MostFrequentErrorsHttpArchiveAggregator implements HttpArchiveAggregator<Map<String, Long>> {
    @NonNull
    @Override
    public String getName() {
        return "most-frequent-errors";
    }

    @Override
    @NonNull
    public Map<String, Long> apply(@NonNull ArrayNode requests) {
        final var errorsCountByUrls =
                StreamSupport.stream(requests.spliterator(), false)
                                .filter(request ->
                                        getFieldValueByPath(request, "response", "status")
                                                        .map(JsonNode::asInt)
                                                        .orElse(0) >= 400
                                )
                                .map(request ->
                                        getFieldValueByPath(request, "request", "url")
                                                        .map(JsonNode::asText)
                                                        .orElse(null)
                                )
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return errorsCountByUrls.entrySet()
                                    .stream()
                                    .sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()))
                                    .limit(5)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}