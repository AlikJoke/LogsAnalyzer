package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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
    private String password;
    @NonNull
    private String hash;
    private boolean active;
    private List<UserSettingsEntity> settings;
}
