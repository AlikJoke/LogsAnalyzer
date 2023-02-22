package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.model.HttpArchiveEntity;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HttpArchiveService {

    @Nonnull
    HttpArchiveEntity create(@Nonnull File harFile);

    void deleteAllByUserKey(@Nonnull String userKey);

    void deleteById(@Nonnull String id);

    @Nonnull
    Optional<HttpArchiveBody> findById(@Nonnull String id);

    @Nonnull
    Map<String, Object> analyze(@Nonnull File harFile);

    @Nonnull
    Map<String, Object> analyze(@Nonnull String harId);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull String id);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull String id, @Nonnull SearchQuery searchQuery);
}
