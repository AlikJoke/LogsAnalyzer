package org.analyzer.logs.service.std;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public record TimestampGap(
        @Nonnegative long interval,
        @NonNull String unit,
        @NonNull String operation) {

    private static final Map<String, TimeUnit> unitMapping = new HashMap<>(){{
        put("h", TimeUnit.HOURS);
        put("m", TimeUnit.MINUTES);
        put("s", TimeUnit.SECONDS);
        put("ms", TimeUnit.MILLISECONDS);
        put("d", TimeUnit.DAYS);
    }};

    @NonNull
    @JsonIgnore
    public TimeUnit getTimeUnit() {
        return unitMapping.get(unit);
    }

    @NonNull
    @JsonIgnore
    public PredicateOperation getPredicateOp() {
        return PredicateOperation.value(operation());
    }
}
