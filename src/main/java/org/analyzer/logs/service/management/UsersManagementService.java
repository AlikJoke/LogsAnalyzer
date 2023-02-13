package org.analyzer.logs.service.management;

import javax.annotation.Nonnull;

public interface UsersManagementService extends MongoDBManagementService {

    void disableUser(@Nonnull String username);

    long count(boolean onlyActive);
}
