package org.analyzer.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;

@JsonSerialize
@JsonAutoDetect
public record ExceptionResource(@NonNull String error) {
}
