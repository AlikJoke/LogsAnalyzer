package org.analyzer.logs.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Accessors(chain = true)
public class UserEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Indexed(unique = true)
    private String username;
    @NonNull
    @ToString.Exclude
    @Field("encoded_password")
    private String encodedPassword;
    @NonNull
    @ToString.Exclude
    @Indexed(unique = true)
    private String hash;
    @Indexed
    private LocalDateTime modified;
    @Version
    private long version;

    private boolean active;

    private UserSettings settings;

    public boolean disable() {
        if (this.active) {
            this.active = false;
            return true;
        }

        return false;
    }

    public boolean enable() {
        if (!this.active) {
            this.active = true;
            return true;
        }

        return false;
    }
}
