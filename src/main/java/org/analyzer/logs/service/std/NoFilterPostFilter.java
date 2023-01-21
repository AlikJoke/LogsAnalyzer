package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.PostFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component(NoFilterPostFilter.NAME)
class NoFilterPostFilter implements PostFilter {

    static final String NAME = "no-filter";

    @NonNull
    @Override
    public Flux<LogRecord> apply(@NonNull Flux<LogRecord> records) {
        return records;
    }

    @NonNull
    @Override
    public Object getParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Class<?> getParametersClass() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }
}
