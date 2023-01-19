package org.parser.app.service.util;

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
}
