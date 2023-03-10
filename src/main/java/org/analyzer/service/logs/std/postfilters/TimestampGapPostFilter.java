package org.analyzer.service.logs.std.postfilters;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.PostFilter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component(TimestampGapPostFilter.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class TimestampGapPostFilter implements PostFilter {

    static final String NAME = "timestamp-gap";

    private TimestampGap parameters;
    private long gapInMillis;
    private PredicateOperation predicateOperation;

    @NonNull
    @Override
    public List<LogRecordEntity> apply(@NonNull List<LogRecordEntity> records) {
        Objects.requireNonNull(this.parameters, "Gap interval isn't specified");

        if (records.size() < 2) {
            return records;
        }

        final var result = new LinkedHashSet<LogRecordEntity>();

        for (var i = 1; i < records.size(); i++) {
            final var recordsBuffer = List.of(records.get(i - 1), records.get(i));
            result.addAll(compareWithFilter(recordsBuffer));
        }

        return List.copyOf(result);
    }

    @NonNull
    @Override
    public TimestampGap getParameters() {
        return this.parameters;
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        this.parameters = (TimestampGap) parameters;
        this.gapInMillis = TimeUnit.MILLISECONDS.convert(this.parameters.interval(), this.parameters.getTimeUnit());
        this.predicateOperation = this.parameters.getPredicateOp();
    }

    @NonNull
    @Override
    public Class<TimestampGap> getParametersClass() {
        return TimestampGap.class;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    private List<LogRecordEntity> compareWithFilter(final List<LogRecordEntity> elements) {
        if (elements.size() < 2) {
            return List.of();
        }

        final var record1 = elements.get(0);
        final var record2 = elements.get(1);

        final var millis1 = getMillisFromDate(record1.getDate()) + getMillisFromTime(record1.getTime());
        final var millis2 = getMillisFromDate(record2.getDate()) + getMillisFromTime(record2.getTime());

        final var diffMillis = millis2 - millis1;

        final var skip = this.predicateOperation.compute(diffMillis, this.gapInMillis);
        return skip ? List.of() : elements;
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
