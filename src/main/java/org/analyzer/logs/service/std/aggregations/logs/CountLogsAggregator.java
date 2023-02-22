package org.analyzer.logs.service.std.aggregations.logs;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.LogsAggregator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Objects;

@Component(CountLogsAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class CountLogsAggregator implements LogsAggregator<Long> {

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
    public Long apply(@NonNull List<LogRecordEntity> records) {
        Objects.requireNonNull(this.parameters, "Count parameters isn't specified");

        return records
                .stream()
                .map(LogRecordEntity.field2FieldValueFunction(this.additionalFilterBy))
                .filter(value -> this.additionalFilterValue == null || Objects.equals(value, this.additionalFilterValue))
                .count();
    }
}
