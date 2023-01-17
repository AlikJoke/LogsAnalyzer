package org.parser.app.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
@JsonAutoDetect
public record LogRecordsCollectionResource(List<String> records) {
}
