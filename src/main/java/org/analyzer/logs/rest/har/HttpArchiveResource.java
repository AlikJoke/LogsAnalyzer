package org.analyzer.logs.rest.har;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;

@JsonSerialize
@JsonAutoDetect
public record HttpArchiveResource(@NonNull String id, @NonNull ObjectNode body) {
}
