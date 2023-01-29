package org.analyzer.logs.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;

import java.util.List;

@JsonAutoDetect
@JsonSerialize
public record AnonymousRootEntrypointResource(@NonNull List<ResourceLink> links) {
}
