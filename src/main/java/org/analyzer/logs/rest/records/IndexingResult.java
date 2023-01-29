package org.analyzer.logs.rest.records;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import org.analyzer.logs.rest.ResourceLink;

import java.util.List;

@JsonAutoDetect
@JsonSerialize
public record IndexingResult(@NonNull String indexingKey, @NonNull List<ResourceLink> links) {
}
