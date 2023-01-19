package org.parser.app.service.std;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.PostFilter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component(TimestampGapPostFilter.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class TimestampGapPostFilter implements PostFilter<TimestampGap> {

    static final String NAME = "timestamp-gap";

    private TimestampGap parameters;
    private long gapInMillis;

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
    public void setParameters(@NonNull TimestampGap parameters) {
        this.parameters = parameters;
        this.gapInMillis = TimeUnit.MILLISECONDS.convert(parameters.interval(), parameters.getTimeUnit());
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

        final LocalDateTime first = elems.get(0).getTimestamp();
        final LocalDateTime second = elems.get(1).getTimestamp();

        final long diffMillis = ChronoUnit.MILLIS.between(first, second);

        // TODO поддержка других операций сравнения
        final boolean skip = diffMillis < this.gapInMillis;
        return skip ? Collections.emptyList() : elems;
    }
}
