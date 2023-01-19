package org.parser.app.service;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface SearchQuery {

    @Nonnull
    String query();

    boolean extendedFormat();

    @Nullable
    Map<String, JsonNode> postFilters();
}
