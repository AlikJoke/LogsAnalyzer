package org.analyzer.logs.service.management;

import lombok.Data;
import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

abstract class MongoDBManagementServiceWithUserCountersBase<T> extends MongoDBManagementServiceBase<T> implements MongoDBManagementServiceWithUserCounters {

    private final UserService userService;

    MongoDBManagementServiceWithUserCountersBase(
            @NonNull UserService userService,
            @NonNull ReactiveMongoTemplate template,
            @NonNull Class<T> entityClass) {
        super(template, entityClass);
        this.userService = userService;
    }

    @NonNull
    @Override
    public Flux<MongoDBManagementServiceWithUserCounters.CountByUsers> countByUsers() {
        return composeCountByUsersFlux(
                group("user_key").count().as("total")
        );
    }

    protected Flux<MongoDBManagementServiceWithUserCounters.CountByUsers> composeCountByUsersFlux(final AggregationOperation groupOperation) {
        final Flux<CountByUsers> countByUsersFlux =
                this.template.aggregate(
                        newAggregation(
                                this.entityClass,
                                groupOperation,
                                project("total").and("user_key").previousOperation(),
                                sort(Sort.Direction.DESC, "total")
                        ),
                        this.entityClass,
                        CountByUsers.class);
        return countByUsersFlux
                .flatMap(data ->
                        this.userService.findByUserHash(data.getUserKey())
                                .map(UserEntity::getUsername)
                                .doOnNext(data::setUser_key)
                                .thenReturn(data)
                );
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
