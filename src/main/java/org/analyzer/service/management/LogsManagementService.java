package org.analyzer.service.management;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LogsManagementService {

    boolean createIndex();

    boolean existsIndex();

    void refreshIndex();

    boolean dropIndex();

    @Nonnull
    Map<String, Object> indexInfo();
}
