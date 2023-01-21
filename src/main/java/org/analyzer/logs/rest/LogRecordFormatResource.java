package org.analyzer.logs.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.analyzer.logs.service.LogRecordFormat;

public record LogRecordFormatResource(
        @NonNull String pattern,
        @NonNull @JsonProperty("date_format") String dateFormat,
        @NonNull @JsonProperty("time_format") String timeFormat) implements LogRecordFormat {
}
