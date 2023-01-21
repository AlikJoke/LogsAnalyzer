package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.PostFilter;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class PostFiltersSequenceBuilder {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private NoFilterPostFilter noFilterPostFilter;

    @NonNull
    public Flux<PostFilter> build(@NonNull Map<String, JsonNode> postFilters) {
        return Flux.fromIterable(postFilters.entrySet())
                    .map(this::createPostFilter)
                    .defaultIfEmpty(this.noFilterPostFilter);
    }

    private PostFilter createPostFilter(final Map.Entry<String, JsonNode> pf) {

        final PostFilter postFilter = this.applicationContext.getBean(pf.getKey(), PostFilter.class);
        final Object parameters = this.jsonConverter.convert(pf.getValue(), postFilter.getParametersClass());
        postFilter.setParameters(parameters);

        return postFilter;
    }
}
