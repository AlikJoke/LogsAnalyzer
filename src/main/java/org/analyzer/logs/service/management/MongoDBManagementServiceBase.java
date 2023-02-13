package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class MongoDBManagementServiceBase<T> implements MongoDBManagementService {

    protected final Class<T> entityClass;
    protected final MongoTemplate template;
    protected final IndexOperations collectionOps;

    @Autowired
    MongoDBManagementServiceBase(@NonNull MongoTemplate template, @NonNull Class<T> entityClass) {
        this.template = template;
        this.entityClass = entityClass;
        this.collectionOps = template.indexOps(entityClass);
    }

    @Override
    public void createCollection() {
        this.template.createCollection(this.entityClass);
    }

    @Override
    public boolean existsCollection() {
        return this.template.collectionExists(this.entityClass);
    }

    @Override
    public void dropCollection() {
        this.template.dropCollection(this.entityClass);
    }

    @NonNull
    @Override
    public Map<String, Object> indexesInfo() {
        return this.collectionOps.getIndexInfo()
                                    .stream()
                                    .map(this::composeIndexInfoMap)
                                    .collect(
                                            Collectors.toMap(
                                                    map -> (String) map.get("name"),
                                                    Function.identity()
                                            )
                                    );
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
                .toList());
        ii.getExpireAfter()
                .ifPresent(expireAfter -> props.put("expire-after", expireAfter));
        return props;
    }
}
