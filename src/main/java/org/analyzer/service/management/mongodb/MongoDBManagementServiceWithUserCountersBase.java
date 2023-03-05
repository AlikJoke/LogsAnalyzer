package org.analyzer.service.management.mongodb;

import lombok.Data;
import lombok.NonNull;
import org.analyzer.service.management.MongoDBManagementServiceWithUserCounters;
import org.analyzer.service.users.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

abstract class MongoDBManagementServiceWithUserCountersBase<T> extends MongoDBManagementServiceBase<T> implements MongoDBManagementServiceWithUserCounters {

    private final UserService userService;

    MongoDBManagementServiceWithUserCountersBase(
            @NonNull UserService userService,
            @NonNull MongoTemplate template,
            @NonNull Class<T> entityClass) {
        super(template, entityClass);
        this.userService = userService;
    }

    @NonNull
    @Override
    public List<MongoDBManagementServiceWithUserCounters.CountByUsers> countByUsers() {
        return composeCountByUsers(
                group("user_key").count().as("total")
        );
    }

    protected List<MongoDBManagementServiceWithUserCounters.CountByUsers> composeCountByUsers(final AggregationOperation groupOperation) {
        final var aggregationResults =
                this.template.aggregate(
                        newAggregation(
                                this.entityClass,
                                groupOperation,
                                project("total").and("user_key").previousOperation(),
                                sort(Sort.Direction.DESC, "total")
                        ),
                        this.entityClass,
                        CountByUsers.class
                );
        return aggregationResults
                        .getMappedResults()
                        .stream()
                        .peek(data -> {
                            final var user = this.userService.findByUserHash(data.getUserKey());
                            data.setUser_key(user.getUsername());
                        })
                        .map(MongoDBManagementServiceWithUserCounters.CountByUsers.class::cast)
                        .toList();
    }

    @Data
    private static class CountByUsers implements MongoDBManagementServiceWithUserCounters.CountByUsers {

        private String user_key;
        private long total;

        @NonNull
        @Override
        public String getUserKey() {
            return user_key;
        }

        @Override
        public long getCount() {
            return total;
        }
    }
}
