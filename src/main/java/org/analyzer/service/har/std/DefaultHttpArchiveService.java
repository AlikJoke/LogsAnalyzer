package org.analyzer.service.har.std;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.analyzer.dao.HttpArchiveRepository;
import org.analyzer.entities.HttpArchiveEntity;
import org.analyzer.i18n.MessageHelper;
import org.analyzer.service.exceptions.EntityNotFoundException;
import org.analyzer.service.exceptions.UnsupportedSearchQueryFormatException;
import org.analyzer.service.har.HttpArchiveAnalyzer;
import org.analyzer.service.har.HttpArchiveBody;
import org.analyzer.service.har.HttpArchiveOperationsQuery;
import org.analyzer.service.har.HttpArchiveService;
import org.analyzer.service.logs.LogsService;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.logs.std.SimpleSearchQuery;
import org.analyzer.service.users.CurrentUserAccessor;
import org.analyzer.service.util.JsonConverter;
import org.analyzer.service.util.UnzipperUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.analyzer.service.har.HttpArchiveBody.getFieldValueByPath;

@Service
public class DefaultHttpArchiveService implements HttpArchiveService {

    private static final LocalDate emptyDate = LocalDate.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private static final String BY_INTERVAL_QUERY =
            "(_exists_:date.keyword AND date.keyword:[%s TO %s] OR date.keyword:%s) AND time.keyword:[%s TO %s]";

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
    @Value("${logs.analyzer.har.trace-id.headers:x-trace-id,trace-id}.split(',')")
    private List<String> traceIdHeaders;
    @Value("${logs.analyzer.har.span-id.headers:x-span-id,span-id}.split(',')")
    private List<String> spanIdHeaders;

    @Override
    public void deleteAll() {
        final var user = this.userAccessor.get();
        this.httpArchiveRepository.deleteAllByUserKey(user.getHash());
    }

    @Override
    public void deleteAllByCreationDate(@NonNull LocalDateTime beforeDate) {
        final var user = this.userAccessor.get();
        this.httpArchiveRepository.deleteAllByUserKeyAndCreationDate(user.getHash(), beforeDate);
    }

    @Override
    public void deleteById(@NonNull String id) {
        this.httpArchiveRepository.deleteById(id);
    }

    @NonNull
    @Override
    public HttpArchiveEntity findById(@NonNull String id) {
        final var entity = this.httpArchiveRepository.findById(id);
        return entity
                    .map(har -> har.setBodyNode(this.jsonConverter.convert(har.getBody().get("json", String.class))))
                    .orElseThrow(() -> new EntityNotFoundException(MessageHelper.getMessage("org.analyzer.har.not.found", id)));
    }

    @NonNull
    @Override
    public List<HttpArchiveEntity> findAll(@NonNull Pageable pageable) {
        final var user = this.userAccessor.get();
        return this.httpArchiveRepository.findAllByUserKey(user.getHash(), pageable);
    }

    @NonNull
    @Override
    public HttpArchiveBody applyOperations(@NonNull String harId, @NonNull HttpArchiveOperationsQuery operationsQuery) {
        final var body = findArchiveBody(harId);
        return operationsQuery.applyTo(body);
    }

    @NonNull
    @Override
    public HttpArchiveBody applyOperations(@NonNull File har, @NonNull HttpArchiveOperationsQuery operationsQuery) {
        final var body = findSingleArchiveBody(har);
        return operationsQuery.applyTo(body);
    }

    @NonNull
    @Override
    public Map<String, Object> analyze(@NonNull File harFileOrArchive, @Nullable HttpArchiveOperationsQuery operationsQuery) {
        final var flatFiles = this.unzipperUtil.flat(harFileOrArchive);
        if (flatFiles.isEmpty()) {
            throw new IllegalStateException(MessageHelper.getMessage("org.analyzer.target.files.not.found", harFileOrArchive.getName()));
        }

        final Map<String, Object> resultByFiles = new HashMap<>(flatFiles.size(), 1);
        flatFiles.forEach(file -> {
            final var body = findSingleArchiveBody(file);
            final var bodyToAnalyze = operationsQuery == null ? body : operationsQuery.applyTo(body);

            resultByFiles.put(file.getName(), this.analyzer.analyze(bodyToAnalyze));
        });

        return resultByFiles;
    }

    @NonNull
    @Override
    public Map<String, Object> analyze(@NonNull String harId, @Nullable HttpArchiveOperationsQuery operationsQuery) {
        final var body = findArchiveBody(harId);
        final var bodyToAnalyze = operationsQuery == null ? body : operationsQuery.applyTo(body);
        return this.analyzer.analyze(bodyToAnalyze);
    }

    @NonNull
    @Override
    public Map<JsonNode, List<String>> groupLogRecordsByRequests(@NonNull String harId, @Nullable HttpArchiveOperationsQuery operationsQuery) {
        final var body = findArchiveBody(harId);
        final var resultBody = operationsQuery == null ? body : operationsQuery.applyTo(body);
        return groupLogRecordsByRequests(resultBody, operationsQuery == null ? null : operationsQuery.additionalLogsSearchQuery());
    }

    @NonNull
    @Override
    public Map<JsonNode, List<String>> groupLogRecordsByRequests(@NonNull File harFileOrArchive, @Nullable HttpArchiveOperationsQuery operationsQuery) {
        final var body = findSingleArchiveBody(harFileOrArchive);
        final var resultBody = operationsQuery == null ? body : operationsQuery.applyTo(body);
        return groupLogRecordsByRequests(resultBody, operationsQuery == null ? null : operationsQuery.additionalLogsSearchQuery());
    }

    @NonNull
    @Override
    public List<HttpArchiveEntity> create(@NonNull File harFileOrArchive) {
        final var flatFiles = this.unzipperUtil.flat(harFileOrArchive);
        if (flatFiles.isEmpty()) {
            throw new IllegalStateException(MessageHelper.getMessage("org.analyzer.target.files.not.found", harFileOrArchive.getName()));
        }

        final List<HttpArchiveEntity> entitiesToSave = new ArrayList<>(flatFiles.size());
        flatFiles.forEach(file -> {
            final var jsonBody = this.jsonConverter.convertFromFile(file);
            final var jsonBodyAsString = this.jsonConverter.convertToJson(jsonBody);
            final var archiveEntity = new HttpArchiveEntity()
                                            .setId(UUID.randomUUID().toString())
                                            .setBody(Document.parse(jsonBodyAsString))
                                            .setCreated(LocalDateTime.now())
                                            .setTitle(file.getName())
                                            .setUserKey(this.userAccessor.get().getHash());

            entitiesToSave.add(archiveEntity);
        });

        return this.httpArchiveRepository.saveAll(entitiesToSave);
    }

    private HttpArchiveBody findSingleArchiveBody(final File har) {
        final var flatFiles = this.unzipperUtil.flat(har);
        if (flatFiles.isEmpty()) {
            throw new IllegalStateException(MessageHelper.getMessage("org.analyzer.target.files.not.found", har.getName()));
        } else if (flatFiles.size() > 1) {
            throw new IllegalStateException(MessageHelper.getMessage("org.analyzer.found.multiple.files", har.getName()));
        }

        final var jsonBody = this.jsonConverter.convertFromFile(flatFiles.get(0));
        return new HttpArchiveBody(har.getName(), jsonBody);
    }

    private HttpArchiveBody findArchiveBody(final String harId) {
        final var har = findById(harId);
        final var bodyNode = har.getBodyNode();
        return new HttpArchiveBody(har.getTitle(), bodyNode);
    }

    private Map<JsonNode, List<String>> groupLogRecordsByRequests(@NonNull HttpArchiveBody harBody, @Nullable SearchQuery searchQuery) {

        if (searchQuery != null && searchQuery.extendedFormat()) {
            throw new UnsupportedSearchQueryFormatException();
        }

        final var entries = (ArrayNode) harBody.getFieldValueByPath("log", "entries").orElseThrow();

        final var baseQuery = searchQuery == null ? "" : ("(" + searchQuery.query() + ") AND ");
        final Map<JsonNode, List<String>> result = new HashMap<>(entries.size(), 1);
        entries.forEach(request -> {
            final var query = new StringBuilder(baseQuery);
            findTraceIdHeader(request)
                    .ifPresentOrElse(
                            traceId -> query.append("traceId.keyword:")
                                            .append(traceId)
                                            .append(findSpanIdHeader(request)
                                                        .map(spanId -> "spanId.keyword:" + spanId)
                                                        .orElse("")
                                            ),
                            () -> {
                                final var interval = getRequestExecutionInterval(request);
                                final var startIntervalDate = LocalDate.ofInstant(interval.getLeft(), ZoneOffset.UTC);
                                final var endIntervalDate = LocalDate.ofInstant(interval.getRight(), ZoneOffset.UTC);
                                final var startIntervalTime = LocalTime.ofInstant(interval.getLeft(), ZoneOffset.UTC);
                                final var endIntervalTime = LocalTime.ofInstant(interval.getRight(), ZoneOffset.UTC);

                                final var formattedIntervalQuery =
                                        BY_INTERVAL_QUERY.formatted(
                                                startIntervalDate.toString(),
                                                endIntervalDate.toString(),
                                                emptyDate.toString(),
                                                startIntervalTime.toString(),
                                                endIntervalTime.toString()
                                        );
                                query.append(formattedIntervalQuery);
                            }
                    );

            final var requestSearchQuery = new SimpleSearchQuery(
                    query.toString(),
                    searchQuery == null ? Map.of() : searchQuery.postFilters(),
                    searchQuery == null ? Map.of() : searchQuery.sorts()
            );

            final List<String> logs = new ArrayList<>(this.logsService.searchByQuery(requestSearchQuery));
            while (logs.addAll(this.logsService.searchByQuery(requestSearchQuery.toNextPageQuery())));

            result.put(request, logs);
        });

        return result;
    }

    private Optional<String> findTraceIdHeader(final JsonNode request) {
        return findRequestHeader(request, this.traceIdHeaders);
    }

    private Optional<String> findSpanIdHeader(final JsonNode request) {
        return findRequestHeader(request, this.spanIdHeaders);
    }

    private Optional<String> findRequestHeader(final JsonNode request, final List<String> possibleHeaderNames) {
        final var headers = getFieldValueByPath(request, "response", "headers")
                                .map(ArrayNode.class::cast)
                                .orElse(null);
        if (headers == null) {
            return Optional.empty();
        }

        for (final var header : headers) {
            if (getFieldValueByPath(header, "name")
                    .map(JsonNode::asText)
                    .map(String::toLowerCase)
                    .filter(possibleHeaderNames::contains)
                    .isPresent()) {
                return getFieldValueByPath(header, "value").map(JsonNode::asText);
            }
        }

        return Optional.empty();
    }

    private Pair<Instant, Instant> getRequestExecutionInterval(final JsonNode request) {

        final long startedDtInMillis = getFieldValueByPath(request, "startedDateTime")
                                        .map(JsonNode::asText)
                                        .map(DateTimeFormatter.ISO_INSTANT::parse)
                                        .map(Instant::from)
                                        .map(Instant::toEpochMilli)
                                        .orElse(0L);

        final var blockedQueueingTime = getTimeFieldValueFromRequest(request, "timings", "_blocked_queueing");
        final var blockedTime = getTimeFieldValueFromRequest(request, "timings", "blocked");
        final var dnsTime = getTimeFieldValueFromRequest(request, "timings", "dns");
        final var sslTime = getTimeFieldValueFromRequest(request, "timings", "ssl");
        final var connectTime = getTimeFieldValueFromRequest(request, "timings", "connect");
        final var receiveTime = getTimeFieldValueFromRequest(request, "timings", "receive");
        final var sendTime = getTimeFieldValueFromRequest(request, "timings", "send");
        final var fullExecutionTime = getTimeFieldValueFromRequest(request, "time");

        final var startExecutionTimeOnServerMs = (long) (startedDtInMillis + blockedQueueingTime + blockedTime + dnsTime + sslTime + connectTime + sendTime);
        final var finishExecutionTimeOnServerMs = (long) (startedDtInMillis + fullExecutionTime - receiveTime);

        return ImmutablePair.of(
                Instant.ofEpochMilli(startExecutionTimeOnServerMs),
                Instant.ofEpochMilli(finishExecutionTimeOnServerMs)
        );
    }

    private double getTimeFieldValueFromRequest(final JsonNode request, final String... path) {
        return getFieldValueByPath(request, path)
                .map(JsonNode::asDouble)
                .filter(value -> value > 0)
                .orElse(0.0);
    }
}
