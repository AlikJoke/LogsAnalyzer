package org.analyzer.service.management;

import javax.annotation.Nonnull;
import java.util.List;

public interface MongoDBManagementServiceWithUserCounters extends MongoDBManagementService {

    long commonCount();

    @Nonnull
    List<CountByUsers> countByUsers();

    interface CountByUsers {

        @Nonnull
        String getUserKey();

        long getCount();
    }
}
