package org.parser.app.service.std;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.Aggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.concurrent.NotThreadSafe;

@Component(FrequencyAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class FrequencyAggregator implements Aggregator {

    static final String NAME = "frequency";

    private Frequency parameters;

    @NonNull
    @Override
    public Frequency getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        this.parameters = (Frequency) parameters;
    }

    @NonNull
    @Override
    public Class<Frequency> getParametersClass() {
        return Frequency.class;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Flux<String> apply(Flux<LogRecord> recordFlux) {
        return null;
    }
}
