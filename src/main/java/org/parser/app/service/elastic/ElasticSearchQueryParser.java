package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.service.SearchQuery;
import org.parser.app.service.SearchQueryParser;
import org.springframework.data.elasticsearch.core.query.StringQuery;
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

    @NonNull
    @Override
    public Mono<StringQuery> parse(@NonNull SearchQuery query) {
        final String resultQueryString = query.extendedFormat() ? query.query() : QUERY_STRING_BODY_TEMPLATE.formatted(query.query());
        return Mono.just(new StringQuery(resultQueryString));
    }
}
