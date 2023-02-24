package org.analyzer.logs.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Sort;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record HttpArchiveBody(@NonNull JsonNode body) {

    public HttpArchiveBody {
        if (body.get("log") == null || !(body.get("log").get("entries") instanceof ArrayNode)) {
            throw new IllegalArgumentException("Unsupported HAR body: " + body);
        }
    }

    @NonNull
    public HttpArchiveBody toSortedByRequestsResponseTime(@Nullable Sort.Order sortBy) {
        final var bodyCopy = body.deepCopy();
        final var entries = (ArrayNode) getFieldValueByPath(bodyCopy, "log", "entries").orElseThrow();
        final List<Pair<JsonNode, Double>> requestsByTime = new ArrayList<>(entries.size());
        entries.forEach(request -> {
            final var responseTime = getTimeFieldValueToSorting(request, sortBy);
            requestsByTime.add(ImmutablePair.of(request, responseTime));
        });

        final var direction = sortBy == null ? Sort.Direction.DESC : sortBy.getDirection();
        final Comparator<Pair<JsonNode, Double>> byTimeComparatorAsc = Comparator.comparingDouble(Pair::getRight);
        entries.removeAll();
        requestsByTime
                .stream()
                .sorted(direction == Sort.Direction.DESC ? byTimeComparatorAsc.reversed() : byTimeComparatorAsc)
                .map(Pair::getLeft)
                .forEach(entries::add);

        return new HttpArchiveBody(bodyCopy);
    }

    @NonNull
    public HttpArchiveBody applyFilterBy(@NonNull String key) {
        final var bodyCopy = body.deepCopy();
        final var entries = (ArrayNode) getFieldValueByPath(bodyCopy, "log", "entries").orElseThrow();
        final List<JsonNode> filteredRequests = new ArrayList<>(entries.size());
        final var keyInLowerCase = key.toLowerCase();
        entries.forEach(request -> {
            final List<JsonNode> fieldsToFilter = new ArrayList<>();
            getFieldValueByPath(request, "request", "url")
                    .ifPresent(fieldsToFilter::add);
            getFieldValueByPath(request, "response", "content", "text")
                    .ifPresent(fieldsToFilter::add);
            getFieldValueByPath(request, "request", "postData", "text")
                    .ifPresent(fieldsToFilter::add);

            if (fieldsToFilter
                    .stream()
                    .map(JsonNode::asText)
                    .anyMatch(value -> value.toLowerCase().contains(keyInLowerCase))) {
                filteredRequests.add(request);
            }
        });

        entries.removeAll().addAll(filteredRequests);

        return new HttpArchiveBody(bodyCopy);
    }

    @NonNull
    public Optional<JsonNode> getFieldValueByPath(@NonNull final String... path) {
        return getFieldValueByPath(body, path);
    }

    @NonNull
    public static Optional<JsonNode> getFieldValueByPath(@NonNull final JsonNode obj, @NonNull final String... path) {

        JsonNode temp = obj;
        for (final String pathPart : path) {
            temp = temp.get(pathPart);
            if (temp == null || temp.isMissingNode()) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(temp == obj ? null : temp);
    }

    private double getTimeFieldValueToSorting(final JsonNode requestNode, final Sort.Order sort) {
        final String defaultSortingField = "time";
        return sort == null || defaultSortingField.equals(sort.getProperty())
                ? getFieldValueByPath(requestNode, defaultSortingField)
                        .map(JsonNode::asDouble)
                        .orElse(0.0)
                : getFieldValueByPath(requestNode, "timings", sort.getProperty())
                        .map(JsonNode::asDouble)
                        .orElse(0.0);
    }
}
