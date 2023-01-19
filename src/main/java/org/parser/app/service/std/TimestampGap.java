package org.parser.app.service.std;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@JsonAutoDetect
@JsonSerialize
public record TimestampGap(@Nonnegative long interval, @NonNull String unit) {

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
}
