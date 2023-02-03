package org.analyzer.logs.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("saved-queries")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Accessors(chain = true)
public class UserSearchQueryEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Indexed(unique = true)
    private String id;
    @NonNull
    private String query;
    @NonNull
    @ToString.Exclude
    @Indexed
    @Field("user_key")
    private String userKey;
    @NonNull
    private LocalDateTime created;
}
