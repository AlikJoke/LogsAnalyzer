package org.analyzer.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
public class JsonConverter {

    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    public JsonConverter() {
        this.mapper = new ObjectMapper();
        this.writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @NonNull
    public <T> T convert(@NonNull JsonNode source, @NonNull Class<T> parametersType) {
        try {
            return mapper.treeToValue(source, parametersType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public JsonNode convert(@NonNull String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public String convertToJson(@NonNull Object object) {
        try {
            return this.writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public JsonNode convertFromFile(@NonNull File file) {
        try {
            return mapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] writeAsBytes(@NonNull Object object) {
        try {
            return this.writer.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
