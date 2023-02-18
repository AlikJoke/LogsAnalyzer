package org.analyzer.logs.service.std.aggregations;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.PostAggregationFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(FrequencyAggregator.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class FrequencyAggregator implements Aggregator<List<Pair<String, Long>>> {

    public static final String NAME = "frequency";

    private final PostAggregationFilter<List<Pair<String, Long>>> postAggregationFilter = values -> {

        final var params = Objects.requireNonNull(getParameters());
        values.sort((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()));
        values.removeIf(value -> value.getValue() < params.minFrequency());

        final var redundantValues = values.subList(0, values.size() > params.takeCount() ? params.takeCount() : values.size());
        values.removeAll(redundantValues);
    };

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

    @NonNull
    @Override
    public Optional<PostAggregationFilter<List<Pair<String, Long>>>> postFilter() {
        return Optional.of(this.postAggregationFilter);
    }

    @Override
    @NonNull
    public List<Pair<String, Long>> apply(@NonNull List<LogRecordEntity> recordList) {
        Objects.requireNonNull(this.parameters, "Frequency parameters isn't specified");

        final Predicate<LogRecordEntity> filterPredicate =
                record -> this.additionalFilterBy == null
                        || Objects.equals(this.additionalFilterValue, LogRecordEntity.field2FieldValueFunction(this.additionalFilterBy).apply(record));

        final var groupingBy = LogRecordEntity.field2FieldValueFunction(this.parameters.groupBy() == null ? "record" : this.parameters.groupBy())
                                                .andThen(String::valueOf);
        return recordList
                .stream()
                .filter(filterPredicate)
                .collect(
                        Collectors.groupingBy(
                                groupingBy,
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().intValue() >= this.parameters.minFrequency())
                .sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()))
                .limit(this.parameters.takeCount() > 0 ? this.parameters.takeCount() : Integer.MAX_VALUE)
                .map(ImmutablePair::of)
                .collect(Collectors.toList());
    }
}
