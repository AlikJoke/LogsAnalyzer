package org.analyzer.logs.service.std;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public record Frequency(
        @JsonProperty("group_by") @Nullable String groupBy,
        @JsonProperty("min_frequency") @Nonnegative int minFrequency,
        @JsonProperty("output_pattern") @Nullable String outputPattern) {
}
