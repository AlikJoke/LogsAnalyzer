package org.analyzer.logs.rest.hateoas;

import lombok.NonNull;
import org.analyzer.logs.rest.ResourceLink;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LinksCollector {

    @NonNull
    public List<ResourceLink> collectFor(@NonNull Class<?> resourceClass) {
        // TODO
        return Collections.emptyList();
    }
}
