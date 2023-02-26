package org.analyzer.rest.har;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import org.analyzer.rest.ResourceLink;

import java.util.List;

@JsonSerialize
@JsonAutoDetect
public record HttpArchiveResource(
        @NonNull String id,
        @NonNull String title,
        @NonNull JsonNode body,
        @NonNull List<ResourceLink> links) {
}
