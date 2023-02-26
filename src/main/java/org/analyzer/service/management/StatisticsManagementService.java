package org.analyzer.service.management;

import javax.annotation.Nonnull;
import java.util.List;

public interface StatisticsManagementService extends MongoDBManagementServiceWithUserCounters {

    @Nonnull
    List<CountByUsers> countRecordsByUsers();
}
