package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.service.SearchQueryParser;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax">...</a>
 */
@Component
public class ElasticSearchQueryParser implements SearchQueryParser<StringQuery> {

    @NonNull
    @Override
    public Mono<StringQuery> parse(@NonNull String queryString) {
        return Mono.just(new StringQuery(queryString));
    }
}
