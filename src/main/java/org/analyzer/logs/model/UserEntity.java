package org.analyzer.logs.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class UserEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private String username;
    @NonNull
    @ToString.Exclude
    @Field("encoded_password")
    private String encodedPassword;
    @NonNull
    @ToString.Exclude
    private String hash;

    private boolean active;

    private UserSettings settings;

    public boolean disable() {
        if (this.active) {
            this.active = false;
            return true;
        }

        return false;
    }
}
