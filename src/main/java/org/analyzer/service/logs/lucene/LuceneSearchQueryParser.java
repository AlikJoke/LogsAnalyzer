package org.analyzer.service.logs.lucene;

import lombok.NonNull;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.logs.SearchQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

public class LuceneSearchQueryParser implements SearchQueryParser<Query> {

    private static final String QUERY_TEMPLATE = "(%s) AND (id.keyword:%s#*)";
    private static final String SOURCE_KEYWORD = "_source";

    @NonNull
    @Override
    public Query parse(@NonNull SearchQuery query, @NonNull String userKey) {
        try (final var analyzer = new StandardAnalyzer()) {
            final var parser = new QueryParser(SOURCE_KEYWORD, analyzer);
            return parser.parse(QUERY_TEMPLATE.formatted(query.query(), userKey));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
