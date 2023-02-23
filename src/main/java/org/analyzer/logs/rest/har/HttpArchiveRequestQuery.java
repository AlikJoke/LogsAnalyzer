package org.analyzer.logs.rest.har;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.analyzer.logs.service.HttpArchiveOperationsQuery;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ToString
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class HttpArchiveRequestQuery implements HttpArchiveOperationsQuery {

    boolean applyDefaultSorting;
    Map<String, Sort.Direction> sort;
    Set<String> filteringKeys;

    @NonNull
    @Override
    public Map<String, Sort.Direction> sort() {
        return this.sort == null ? Collections.emptyMap() : this.sort;
    }

    @NonNull
    @Override
    public Set<String> filteringKeys() {
        return this.filteringKeys == null ? Collections.emptySet() : this.filteringKeys;
    }
}
