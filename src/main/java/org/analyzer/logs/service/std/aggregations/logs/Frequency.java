package org.analyzer.logs.service.std.aggregations.logs;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Map;

public record Frequency(
        @JsonProperty("group_by") @Nullable String groupBy,
        @JsonProperty("min_frequency") @Nonnegative int minFrequency,
        @JsonProperty("additional_filter") @Nullable Map<String, Object> additionalFilter,
        @JsonProperty("take_count") int takeCount) {
}
