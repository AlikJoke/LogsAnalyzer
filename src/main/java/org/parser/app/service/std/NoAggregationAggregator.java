package org.parser.app.service.std;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.Aggregator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component(NoAggregationAggregator.NAME)
class NoAggregationAggregator implements Aggregator {

    static final String NAME = "no-aggregation";

    @NonNull
    @Override
    public Flux<String> apply(@NonNull Flux<LogRecord> records) {
        return records.map(LogRecord::getSource);
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
