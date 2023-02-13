package org.analyzer.logs.service.management;

import javax.annotation.Nonnull;
import java.util.Map;

public interface MongoDBManagementService {

    void createCollection();

    boolean existsCollection();

    void dropCollection();

    @Nonnull
    Map<String, Object> indexesInfo();
}
