package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.model.HttpArchiveEntity;
import org.analyzer.logs.service.HttpArchiveService;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultHttpArchiveService implements HttpArchiveService {

    @Autowired
    private HttpArchiveService httpArchiveService;
    @Autowired
    private LogsService logsService;

    @Override
    public void deleteAllByUserKey(@NonNull String userKey) {
        this.httpArchiveService.deleteAllByUserKey(userKey);
    }

    @Override
    public void deleteById(@NonNull String id) {
        this.httpArchiveService.deleteById(id);
    }

    @NonNull
    @Override
    public Optional<HttpArchiveBody> findById(@NonNull String id) {
        return this.httpArchiveService.findById(id);
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
    public HttpArchiveEntity create(@NonNull File harFile) {
        // TODO
        return null;
    }
}
