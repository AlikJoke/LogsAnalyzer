package org.analyzer.logs.service.std.aggregations.har;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.springframework.stereotype.Component;

@Component
public class RequestsCountHttpArchiveAggregator implements HttpArchiveAggregator<Integer> {
    @NonNull
    @Override
    public String getName() {
        return "requests-count";
    }

    @Override
    @NonNull
    public Integer apply(@NonNull ArrayNode requests) {
        return requests.size();
    }
}
