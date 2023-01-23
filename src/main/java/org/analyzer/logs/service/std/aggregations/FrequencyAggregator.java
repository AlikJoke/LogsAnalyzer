package org.analyzer.logs.service.std.aggregations;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.Aggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.function.Predicate;

@Component(FrequencyAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class FrequencyAggregator implements Aggregator<Tuple2<String, Long>> {

    public static final String NAME = "frequency";

    private Frequency parameters;

    private String additionalFilterBy;
    private Object additionalFilterValue;

    @NonNull
    @Override
    public Frequency getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        this.parameters = (Frequency) parameters;
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
    public Class<Frequency> getParametersClass() {
        return Frequency.class;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @NonNull
    public Flux<Tuple2<String, Long>> apply(@NonNull Flux<LogRecordEntity> recordFlux) {
        Objects.requireNonNull(this.parameters, "Frequency parameters isn't specified");

        final Predicate<LogRecordEntity> filterPredicate =
                record -> this.additionalFilterBy == null
                        || Objects.equals(this.additionalFilterValue, LogRecordEntity.field2FieldValueFunction(this.additionalFilterBy).apply(record));

        return recordFlux
                .filter(filterPredicate)
                .groupBy(LogRecordEntity.field2FieldValueFunction(this.parameters.groupBy() == null ? "record" : this.parameters.groupBy()))
                .flatMap(
                        group -> Mono
                                    .just(group.key().toString())
                                    .zipWith(group.count())
                )
                .filter(tuple -> tuple.getT2().intValue() >= this.parameters.minFrequency())
                .sort((o1, o2) -> Long.compare(o2.getT2(), o1.getT2()))
                .take(this.parameters.takeCount() > 0 ? this.parameters.takeCount() : Integer.MAX_VALUE);
    }
}
