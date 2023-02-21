package org.analyzer.logs.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;
import lombok.experimental.Accessors;
import org.bson.json.JsonObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("har")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Accessors(chain = true)
public class HttpArchiveEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Indexed
    private String id;
    @NonNull
    private LocalDateTime created;
    @NonNull
    @ToString.Exclude
    @Field
    private JsonObject body;
    @NonNull
    @Field("user_key")
    private String userKey;
    private String title;

    @NonNull
    public HttpArchiveBody getJsonBodyWrapper(@NonNull final ObjectMapper mapper) {
        try {
            final var json = mapper.readTree(getBody().getJson());
            return new HttpArchiveBody((ObjectNode) json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
