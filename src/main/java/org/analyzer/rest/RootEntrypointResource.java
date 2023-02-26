package org.analyzer.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;

import java.util.List;

@JsonAutoDetect
@JsonSerialize
public record RootEntrypointResource(@NonNull List<ResourceLink> links) {
}
