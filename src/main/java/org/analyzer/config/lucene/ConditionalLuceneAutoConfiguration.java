package org.analyzer.config.lucene;

import org.analyzer.dao.LogsStorage;
import org.analyzer.dao.lucene.LuceneLogsStorage;
import org.analyzer.service.logs.SearchQueryParser;
import org.analyzer.service.logs.lucene.LuceneSearchQueryParser;
import org.apache.lucene.search.Query;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import static org.analyzer.LogsAnalyzerApplication.BOX_MODE;
import static org.analyzer.LogsAnalyzerApplication.RUN_MODE_PROPERTY;

@AutoConfiguration
@ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = BOX_MODE)
public class ConditionalLuceneAutoConfiguration {

    @Bean
    public SearchQueryParser<Query> luceneSearchQueryParser() {
        return new LuceneSearchQueryParser();
    }

    @Bean
    public LogsStorage luceneLogsStorage() {
        return new LuceneLogsStorage();
    }
}
