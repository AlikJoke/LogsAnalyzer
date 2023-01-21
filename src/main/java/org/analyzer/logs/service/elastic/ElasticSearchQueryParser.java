package org.analyzer.logs.service.elastic;

import lombok.NonNull;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.SearchQueryParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.StringQueryBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax">...</a>
 */
@Component
public class ElasticSearchQueryParser implements SearchQueryParser<StringQuery> {

    private static final String QUERY_STRING_BODY_TEMPLATE = """
            {"query_string":
                {
                    "query": "%s"
                }
            }""";

    @Value("${elasticsearch.default.max_results:10000}")
    private int maxResultsDefault;

    @NonNull
    @Override
    public Mono<StringQuery> parse(@NonNull SearchQuery query) {
        return Mono.fromSupplier(() -> {
            final String resultQueryString = query.extendedFormat() ? query.query() : QUERY_STRING_BODY_TEMPLATE.formatted(query.query());

            final Sort sort = query.sorts()
                                    .entrySet()
                                    .stream()
                                    .map(e -> Sort.by(e.getValue(), e.getKey()))
                                    .reduce(Sort::and)
                                    .orElse(Sort.unsorted());

            return new StringQueryBuilder(resultQueryString)
                        .withMaxResults(query.maxResults() == 0 ? maxResultsDefault : query.maxResults())
                        .withSort(sort)
                        .build();
        });
    }
}
