package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.service.SearchQueryParser;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Commands:
 * -c:"str"
 * -eq:"str"
 * -between:"dt1"-"dt2"
 * -timestamp-gap|>|<|=|>=|<=|10ms|s|m|h
 * Areas:
 * -f:"str"
 * -all
 * Operations:
 * OR
 * AND
 * Examples:
 * -eq:"DEBUG" -f:"level" AND -between:"2022-01-10"-"2022-01-11" -f:"timestamp" AND -eq:"thread-1" -f:"thread"
 * AND -c:"121212222332" -f:"record"
 * -c:"Some text" -all
 */
@Component
public class ElasticSearchQueryParser implements SearchQueryParser<CriteriaQuery> {

    @NonNull
    @Override
    public Mono<CriteriaQuery> parse(@NonNull String queryString) {
        // TODO
        return Mono.empty();
    }
}
