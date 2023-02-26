package org.analyzer.rest.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.analyzer.service.logs.LogRecordFormat;

public record LogRecordFormatResource(
        String pattern,
        @JsonProperty("date_format") String dateFormat,
        @JsonProperty("time_format") String timeFormat) implements LogRecordFormat {
}
