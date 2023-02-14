package org.analyzer.logs.service.elastic;

import lombok.NonNull;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.SearchQueryParser;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.StringQueryBuilder;
import org.springframework.stereotype.Component;

/**
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax">...</a>
 */
@Component
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

    @Value("${elasticsearch.default.max_results:1000}")
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
                .map(e -> Sort.by(e.getValue(), e.getKey()))
                .reduce(Sort::and)
                .orElse(Sort.unsorted());

        return new StringQueryBuilder(resultQueryString)
                .withPageable(PageRequest.of(query.pageNumber(), query.pageSize() == 0 ? maxResultsDefault : query.pageSize(), sort))
                .build();
    }
}
