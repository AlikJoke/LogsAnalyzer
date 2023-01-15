package org.parser.app.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonAutoDetect
public record RequestQuery(String query) {
}
