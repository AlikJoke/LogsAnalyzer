package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveEntity;

import javax.annotation.Nonnull;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HttpArchiveService {

    @Nonnull
    List<HttpArchiveEntity> create(@Nonnull File harFile);

    void deleteAllByUserKey(@Nonnull String userKey);

    void deleteAllByUserKeyAndCreationDate(
            @NonNull String userKey,
            @NonNull LocalDateTime beforeDate);

    void deleteById(@Nonnull String id);

    @Nonnull
    Optional<HttpArchiveEntity> findById(@Nonnull String id);

    @Nonnull
    Map<String, Object> analyze(@Nonnull File harFile);

    @Nonnull
    Map<String, Object> analyze(@Nonnull String harId);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull String harId);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull String harId, @Nonnull SearchQuery searchQuery);
}
