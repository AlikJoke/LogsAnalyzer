package org.analyzer.rest.queries;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.analyzer.entities.UserSearchQueryEntity;
import org.analyzer.rest.ResourceLink;
import org.analyzer.rest.hateoas.LinksCollector;

import java.time.LocalDateTime;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
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
