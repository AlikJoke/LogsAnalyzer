package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.model.HttpArchiveEntity;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HttpArchiveService {

    @Nonnull
    List<HttpArchiveEntity> create(@Nonnull File harFile);

    void deleteAll();

    void deleteAllByUserKeyAndCreationDate(
            @NonNull String userKey,
            @NonNull LocalDateTime beforeDate);

    void deleteById(@Nonnull String id);

    @Nonnull
    HttpArchiveEntity findById(@Nonnull String id);

    @Nonnull
    List<HttpArchiveEntity> findAll(@Nonnull Pageable pageable);

    @Nonnull
    HttpArchiveBody applyOperations(@Nonnull String harId, @Nonnull HttpArchiveOperationsQuery operationsQuery);

    @Nonnull
    HttpArchiveBody applyOperations(@Nonnull File har, @Nonnull HttpArchiveOperationsQuery operationsQuery);

    @Nonnull
    Map<String, Object> analyze(@Nonnull File harFile, @Nullable HttpArchiveOperationsQuery operationsQuery);

    @Nonnull
    Map<String, Object> analyze(@Nonnull String harId, @Nullable HttpArchiveOperationsQuery operationsQuery);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull String harId, @Nullable HttpArchiveOperationsQuery operationsQuery);

    @Nonnull
    Map<JsonNode, List<String>> groupLogRecordsByRequests(@Nonnull File har, @Nullable HttpArchiveOperationsQuery operationsQuery);
}
