package org.analyzer.logs.rest.queries;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import org.analyzer.logs.rest.ResourceLink;

import java.util.List;

@JsonSerialize
@JsonAutoDetect
public record UserQueriesCollection(
        @NonNull List<UserQueryResource> queries,
        @NonNull List<ResourceLink> links) {
}
