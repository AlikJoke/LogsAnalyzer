package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class MongoDBManagementServiceBase<T> implements MongoDBManagementService {

    protected final Class<T> entityClass;
    protected final ReactiveMongoTemplate template;
    protected final ReactiveIndexOperations collectionOps;

    @Autowired
    MongoDBManagementServiceBase(@NonNull ReactiveMongoTemplate template, @NonNull Class<T> entityClass) {
        this.template = template;
        this.entityClass = entityClass;
        this.collectionOps = template.indexOps(entityClass);
    }

    @NonNull
    @Override
    public Mono<Void> createCollection() {
        return this.template.createCollection(this.entityClass).then();
    }

    @NonNull
    @Override
    public Mono<Boolean> existsCollection() {
        return this.template.collectionExists(this.entityClass);
    }

    @NonNull
    @Override
    public Mono<Void> dropCollection() {
        return this.template.dropCollection(this.entityClass);
    }

    @NonNull
    @Override
    public Mono<Map<String, Object>> indexesInfo() {
        return this.collectionOps.getIndexInfo()
                                    .map(this::composeIndexInfoMap)
                                    .collectMap(map -> (String) map.get("name"), Function.identity());
    }

    private Map<String, Object> composeIndexInfoMap(final IndexInfo ii) {
        final Map<String, Object> props = new HashMap<>();
        props.put("name", ii.getName());
        props.put("hashed", ii.isHashed());
        props.put("sparse", ii.isSparse());
        props.put("unique", ii.isUnique());
        props.put("index-fields", ii.getIndexFields()
                .stream()
                .map(IndexField::toString)
                .collect(Collectors.toList()));
        ii.getExpireAfter()
                .ifPresent(expireAfter -> props.put("expire-after", expireAfter));
        return props;
    }
}
