package org.analyzer.logs.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.stereotype.Component;


@Component
public class JsonConverter {

    private static JsonConverter instance;

    private final ObjectMapper mapper = new ObjectMapper();

    public static JsonConverter get() {
        return instance;
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
    public String convertToJson(@NonNull Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    private void init() {
        instance = this;
    }
}
