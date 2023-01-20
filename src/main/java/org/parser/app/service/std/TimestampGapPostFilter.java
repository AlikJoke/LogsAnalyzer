package org.parser.app.service.std;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.PostFilter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
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
    public Flux<LogRecord> apply(@NonNull Flux<LogRecord> records) {
        if (this.parameters == null) {
            throw new IllegalStateException("Gap interval not specified");
        }

        return records
                    .cache(1)
                    .buffer(2, 1)
                    .flatMapIterable(this::compareWithFilter)
                    .distinctUntilChanged();
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

    private List<LogRecord> compareWithFilter(final List<LogRecord> elems) {
        if (elems.size() < 2) {
            return Collections.emptyList();
        }

        final LogRecord record1 = elems.get(0);
        final LogRecord record2 = elems.get(1);

        final long millis1 = getMillisFromDate(record1.getDate()) + getMillisFromTime(record1.getTime());
        final long millis2 = getMillisFromDate(record2.getDate()) + getMillisFromTime(record2.getTime());

        final long diffMillis = millis2 - millis1;

        final boolean skip = this.predicateOperation.compute(diffMillis, this.gapInMillis);
        return skip ? Collections.emptyList() : elems;
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
