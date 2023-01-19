package org.parser.app.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.parser.app.service.PostFilter;
import org.parser.app.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.Nullable;
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
    public Flux<PostFilter<?>> build(@Nullable Map<String, JsonNode> postFilters) {
        if (postFilters == null || postFilters.isEmpty()) {
            return Flux.just(noFilterPostFilter);
        } else {
            return Flux.fromIterable(postFilters.entrySet())
                        .map(this::createPostFilter);
        }
    }

    private PostFilter<?> createPostFilter(final Map.Entry<String, JsonNode> pf) {

        @SuppressWarnings("unchecked")
        final PostFilter<Object> postFilter = this.applicationContext.getBean(pf.getKey(), PostFilter.class);
        final Object parameters = this.jsonConverter.convert(pf.getValue(), postFilter.getParametersClass());
        postFilter.setParameters(parameters);

        return postFilter;
    }
}
