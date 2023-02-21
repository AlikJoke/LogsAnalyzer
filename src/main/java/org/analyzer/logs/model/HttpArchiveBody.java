package org.analyzer.logs.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

import java.util.Map;

public record HttpArchiveBody(@NonNull ObjectNode body) {

    @NonNull
    public HttpArchiveBody toSortedByRequestsResponseTime(@NonNull Sort.Direction direction) {
        // TODO
        return null;
    }

    @NonNull
    public HttpArchiveBody applyFilterBy(@NonNull String key) {
        // TODO
        return null;
    }

    @NonNull
    public Map<String, Object> analyze() {
        // TODO
        return null;
    }
}
