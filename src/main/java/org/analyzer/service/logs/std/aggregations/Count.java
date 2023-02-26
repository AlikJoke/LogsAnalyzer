package org.analyzer.service.logs.std.aggregations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record Count(@JsonProperty("additional_filter") Map<String, Object> additionalFilter) {
}
