package org.analyzer.logs.rest.queries;

import lombok.NonNull;
import lombok.Value;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.analyzer.logs.rest.ResourceLink;
import org.analyzer.logs.rest.hateoas.LinksCollector;

import java.time.LocalDateTime;
import java.util.List;

@Value
public class UserQueryResource {

    @NonNull
    String queryId;
    @NonNull
    LocalDateTime createdAt;
    @NonNull
    String queryJson;
    @NonNull
    List<ResourceLink> links;

    public UserQueryResource(
            @NonNull final UserSearchQueryEntity userSearchQuery,
            @NonNull final LinksCollector linksCollector) {
        this.queryId = userSearchQuery.getId();
        this.queryJson = userSearchQuery.getQuery();
        this.createdAt = userSearchQuery.getCreated();
        this.links = linksCollector.collectFor(UserQueryResource.class);
    }
}
