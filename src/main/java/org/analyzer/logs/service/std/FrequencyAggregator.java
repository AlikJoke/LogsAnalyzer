package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.Aggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.function.Function;

@Component(FrequencyAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class FrequencyAggregator implements Aggregator<Tuple2<String, Long>> {

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
    @NonNull
    public Flux<Tuple2<String, Long>> apply(@NonNull Flux<LogRecord> recordFlux) {
        Objects.requireNonNull(this.parameters, "Frequency parameters isn't specified");

        return recordFlux
                .groupBy(getAggregatedFieldValueFunc())
                .flatMap(
                        group -> Mono
                                    .just(group.key().toString())
                                    .zipWith(group.count())
                )
                .filter(tuple -> tuple.getT2().intValue() >= this.parameters.minFrequency())
                .sort((o1, o2) -> Long.compare(o2.getT2(), o1.getT2()));
    }

    @NonNull
    private Function<LogRecord, Object> getAggregatedFieldValueFunc() {
        final String field = this.parameters.groupBy() == null ? "record" : this.parameters.groupBy();
        return switch (field) {
            case "thread", "thread.keyword" -> LogRecord::getThread;
            case "category", "category.keyword" -> LogRecord::getCategory;
            case "record", "record.keyword" -> record -> "[" + record.getCategory() + "] " + record.getRecord();
            case "date", "date.keyword" -> LogRecord::getDate;
            case "time", "time.keyword" -> LogRecord::getTime;
            case "level", "level.keyword" -> LogRecord::getLevel;
            default -> throw new IllegalArgumentException("Unsupported field aggregator: " + field);
        };
    }
}
