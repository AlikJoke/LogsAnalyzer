package org.analyzer.logs.service.std.aggregations;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.Aggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

@Component(CountAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class CountAggregator implements Aggregator<Long> {

    public static final String NAME = "count";

    private Count parameters;

    private String additionalFilterBy = "record";
    private Object additionalFilterValue;

    @NonNull
    @Override
    public Count getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        this.parameters = (Count) parameters;
        if (!CollectionUtils.isEmpty(this.parameters.additionalFilter())) {
            this.parameters
                    .additionalFilter()
                    .forEach((k, v) -> {
                        this.additionalFilterBy = k;
                        this.additionalFilterValue = v;
                    });
        }
    }

    @NonNull
    @Override
    public Class<Count> getParametersClass() {
        return Count.class;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @NonNull
    public Flux<Long> apply(@NonNull Flux<LogRecordEntity> recordFlux) {
        Objects.requireNonNull(this.parameters, "Count parameters isn't specified");

        return recordFlux
                .map(LogRecordEntity.field2FieldValueFunction(this.additionalFilterBy))
                .filter(value -> Objects.equals(value, this.additionalFilterValue))
                .count()
                .flux();
    }
}
