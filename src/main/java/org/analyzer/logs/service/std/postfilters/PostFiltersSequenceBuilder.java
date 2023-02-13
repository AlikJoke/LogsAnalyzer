package org.analyzer.logs.service.std.postfilters;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.PostFilter;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<PostFilter> build(@NonNull Map<String, JsonNode> postFilters) {
        final var entries = postFilters.entrySet();
        return entries.isEmpty()
                ? List.of(this.noFilterPostFilter)
                : entries
                    .stream()
                    .map(this::createPostFilter)
                    .toList();
    }

    private PostFilter createPostFilter(final Map.Entry<String, JsonNode> pf) {

        final var postFilter = this.applicationContext.getBean(pf.getKey(), PostFilter.class);
        final var parameters = this.jsonConverter.convert(pf.getValue(), postFilter.getParametersClass());
        postFilter.setParameters(parameters);

        return postFilter;
    }
}
