package org.analyzer.service.users;

import org.analyzer.entities.UserEntity;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface UserDataStorageCleaner {

    void clear(@Nonnull UserEntity user, @Nonnull LocalDateTime deleteOlderThan);
}
