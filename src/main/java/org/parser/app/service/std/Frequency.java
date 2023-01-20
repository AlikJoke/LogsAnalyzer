package org.parser.app.service.std;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public record Frequency(
        @Nullable String field,
        @JsonProperty("more_than") @Nonnegative int onlyMoreThan,
        @JsonProperty("output_pattern") @Nullable String outputPattern) {
}
