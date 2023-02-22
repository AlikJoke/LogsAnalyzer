package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import org.analyzer.logs.dao.HttpArchiveRepository;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.model.HttpArchiveEntity;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.exceptions.EntityNotFoundException;
import org.analyzer.logs.service.util.JsonConverter;
import org.analyzer.logs.service.util.UnzipperUtil;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DefaultHttpArchiveService implements HttpArchiveService {

    @Autowired
    private HttpArchiveRepository httpArchiveRepository;
    @Autowired
    private LogsService logsService;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private HttpArchiveAnalyzer analyzer;
    @Autowired
    private UnzipperUtil unzipperUtil;
    @Autowired
    private CurrentUserAccessor userAccessor;

    @Override
    public void deleteAllByUserKey(@NonNull String userKey) {
        this.httpArchiveRepository.deleteAllByUserKey(userKey);
    }

    @Override
    public void deleteAllByUserKeyAndCreationDate(@NonNull String userKey, @NonNull LocalDateTime beforeDate) {
        this.httpArchiveRepository.deleteAllByUserKeyAndCreationDate(userKey, beforeDate);
    }

    @Override
    public void deleteById(@NonNull String id) {
        this.httpArchiveRepository.deleteById(id);
    }

    @NonNull
    @Override
    public Optional<HttpArchiveEntity> findById(@NonNull String id) {
        return this.httpArchiveRepository.findById(id);
    }

    @NonNull
    @Override
    public Map<String, Object> analyze(@NonNull File harFileOrArchive) {
        final var flatFiles = this.unzipperUtil.flat(harFileOrArchive);
        if (flatFiles.isEmpty()) {
            throw new IllegalStateException("Files to analyze not found");
        }

        final Map<String, Object> resultByFiles = new HashMap<>(flatFiles.size(), 1);
        flatFiles.forEach(file -> {
            final var jsonBody = this.jsonConverter.convertFromFile(flatFiles.get(0));
            final var body = new HttpArchiveBody((ObjectNode) jsonBody);

            resultByFiles.put(file.getName(), this.analyzer.analyze(body));
        });

        return resultByFiles;
    }

    @NonNull
    @Override
    public Map<String, Object> analyze(@NonNull String harId) {
        return this.findById(harId)
                    .map(HttpArchiveEntity::getBody)
                    .map(JsonObject::getJson)
                    .map(this.jsonConverter::convert)
                    .map(ObjectNode.class::cast)
                    .map(HttpArchiveBody::new)
                    .map(this.analyzer::analyze)
                    .orElseThrow(() -> new EntityNotFoundException("HAR not found by id: " + harId));
    }

    @NonNull
    @Override
    public Map<JsonNode, List<String>> groupLogRecordsByRequests(@NonNull String id) {
        // TODO
        return null;
    }

    @NonNull
    @Override
    public Map<JsonNode, List<String>> groupLogRecordsByRequests(@NonNull String id, @NonNull SearchQuery searchQuery) {
        // TODO
        return null;
    }

    @NonNull
    @Override
    public List<HttpArchiveEntity> create(@NonNull File harFileOrArchive) {
        final var flatFiles = this.unzipperUtil.flat(harFileOrArchive);
        if (flatFiles.isEmpty()) {
            throw new IllegalStateException("Files to analyze not found");
        }

        final List<HttpArchiveEntity> entitiesToSave = new ArrayList<>(flatFiles.size());
        flatFiles.forEach(file -> {
            final var jsonBody = this.jsonConverter.convertFromFile(flatFiles.get(0));
            final var jsonBodyAsString = this.jsonConverter.convertToJson(jsonBody);
            final var archiveEntity = new HttpArchiveEntity()
                                            .setId(UUID.randomUUID().toString())
                                            .setBody(new JsonObject(jsonBodyAsString))
                                            .setCreated(LocalDateTime.now())
                                            .setTitle(file.getName())
                                            .setUserKey(this.userAccessor.get().getHash());

            entitiesToSave.add(archiveEntity);
        });

        return this.httpArchiveRepository.saveAll(entitiesToSave);
    }
}
