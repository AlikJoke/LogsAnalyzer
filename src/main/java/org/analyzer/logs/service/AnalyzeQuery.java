package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface AnalyzeQuery extends SearchQuery {

    @Nonnull
    Map<String, JsonNode> aggregations();

    boolean save();

    @Nullable
    String analyzeResultName();

    @Nonnull
    SearchQuery toSearchQuery();

    @Nonnull
    String getId();
}
