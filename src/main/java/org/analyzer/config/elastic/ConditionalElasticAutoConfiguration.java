package org.analyzer.config.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.analyzer.dao.LogsStorage;
import org.analyzer.dao.elastic.ElasticLogsStorage;
import org.analyzer.service.logs.SearchQueryParser;
import org.analyzer.service.logs.elastic.ElasticSearchQueryParser;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import static org.analyzer.LogsAnalyzerApplication.DISTRIBUTED_MODE;
import static org.analyzer.LogsAnalyzerApplication.RUN_MODE_PROPERTY;

@AutoConfiguration
@EnableElasticsearchRepositories(basePackages = "org.analyzer.dao")
@ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = DISTRIBUTED_MODE, matchIfMissing = true)
public class ConditionalElasticAutoConfiguration {

    @Bean
    public ElasticsearchClient client(RestClientBuilder restClientBuilder) {
        return ElasticsearchClients.createImperative(restClientBuilder.build());
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
        return new MappingElasticsearchConverter(mappingContext);
    }

    @Bean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient client, ElasticsearchConverter converter) {
        return new ElasticsearchTemplate(client, converter);
    }

    @Bean
    public SearchQueryParser<StringQuery> elasticSearchQueryParser() {
        return new ElasticSearchQueryParser();
    }

    @Bean
    public LogsStorage elasticLogsStorage() {
        return new ElasticLogsStorage();
    }
}
