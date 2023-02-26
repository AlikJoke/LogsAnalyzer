package org.analyzer.service.users;

import org.analyzer.entities.UserEntity;

import javax.annotation.Nonnull;

public interface CurrentUserAccessor {

    @Nonnull
    UserEntity get();

    @Nonnull
    UserContext as(@Nonnull String userKey);

    @Nonnull
    UserContext as(@Nonnull UserEntity user);

    interface UserContext extends AutoCloseable {

        @Override
        void close();
    }
}
