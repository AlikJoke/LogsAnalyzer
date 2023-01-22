package org.analyzer.logs.service.std.aggregations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record Count(@JsonProperty("additional_filter") Map<String, Object> additionalFilter) {
}
