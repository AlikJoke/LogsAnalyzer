package org.analyzer.logs.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.stereotype.Component;


@Component
public class JsonConverter {

    private final ObjectMapper mapper = new ObjectMapper();

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
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] writeAsBytes(@NonNull Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
