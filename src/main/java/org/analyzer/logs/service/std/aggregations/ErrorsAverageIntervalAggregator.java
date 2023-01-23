package org.analyzer.logs.service.std.aggregations;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.Aggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component(ErrorsAverageIntervalAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class ErrorsAverageIntervalAggregator implements Aggregator<Double> {

    public static final String NAME = "errors-average-interval";

    @NonNull
    @Override
    public Object getParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParameters(@NonNull Object parameters) {

    }

    @NonNull
    @Override
    public Class<Object> getParametersClass() {
        return Object.class;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @NonNull
    public Flux<Double> apply(@NonNull Flux<LogRecordEntity> recordFlux) {
        return recordFlux
                .filter(record -> LogLevel.ERROR.name().equalsIgnoreCase(record.getLevel()))
                .cache(1)
                .buffer(2, 1)
                .map(this::getDiffInterval)
                .collect(Collectors.averagingLong(i -> i))
                .flux();
    }

    private long getDiffInterval(final List<LogRecordEntity> records) {
        if (records.size() < 2) {
            return 0;
        }

        final var record1 = records.get(0);
        final var record2 = records.get(1);

        final var millis1 = getMillisFromDate(record1.getDate()) + getMillisFromTime(record1.getTime());
        final var millis2 = getMillisFromDate(record2.getDate()) + getMillisFromTime(record2.getTime());

        return millis2 - millis1;
    }

    private long getMillisFromDate(final LocalDate date) {
        return date == null ? 0 : TimeUnit.MILLISECONDS.convert(date.toEpochDay(), TimeUnit.DAYS);
    }

    private long getMillisFromTime(final LocalTime time) {
        return time == null
                ? 0
                : TimeUnit.MILLISECONDS.convert(time.toSecondOfDay(), TimeUnit.SECONDS)
                + TimeUnit.MILLISECONDS.convert(time.getNano(), TimeUnit.NANOSECONDS);
    }
}
