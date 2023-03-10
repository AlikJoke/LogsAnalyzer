package org.analyzer.service.logs.elastic;

import lombok.NonNull;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.logs.SearchQueryParser;
import org.analyzer.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.StringQueryBuilder;

import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

/**
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax">...</a>
 */
public class ElasticSearchQueryParser implements SearchQueryParser<StringQuery> {

    private static final String QUERY_STRING_BODY_TEMPLATE = """
            {"query_string":
                {
                    "query": "(%s) AND (id.keyword:%s#*)"
                }
            }""";

    private static final String QUERY_FULL_FILTERED_TEMPLATE = """
            {"filtered":
                {
                    "filter":
                        {
                            "prefix":
                                {
                                    "id.keyword": "%s#"
                                }
                        },
                    %s
                }
            }""";

    @Value("${logs.analyzer.search.default.max_results}")
    private int maxResultsDefault;
    @Autowired
    private JsonConverter jsonConverter;

    @NonNull
    @Override
    public StringQuery parse(@NonNull SearchQuery query, @NonNull String userKey) {

        final var resultQueryString = query.extendedFormat()
                ? QUERY_FULL_FILTERED_TEMPLATE.formatted(userKey, query.query())
                : QUERY_STRING_BODY_TEMPLATE.formatted(query.query(), userKey);

        final var sort = query.sorts()
                .entrySet()
                .stream()
                .map(e -> Sort.by(e.getValue(), toStorageFieldName(e.getKey())))
                .reduce(Sort::and)
                .orElse(
                        Sort.by(
                                Sort.Order.asc(toStorageFieldName("date")).nullsLast(),
                                Sort.Order.asc(toStorageFieldName("time")).nullsLast()
                        )
                );

        final var maxSize = query.pageSize() == 0 || query.pageSize() > maxResultsDefault ? maxResultsDefault : query.pageSize();
        return new StringQueryBuilder(resultQueryString)
                .withPageable(PageRequest.of(query.pageNumber(), maxSize, sort))
                .build();
    }
}
