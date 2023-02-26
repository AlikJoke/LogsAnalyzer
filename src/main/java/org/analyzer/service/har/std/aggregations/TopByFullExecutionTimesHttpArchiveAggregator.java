package org.analyzer.service.har.std.aggregations;

import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TopByFullExecutionTimesHttpArchiveAggregator extends TopByExecutionTimesHttpArchiveAggregator {
    @NonNull
    @Override
    public String getName() {
        return "requests-top-by-execution-times";
    }

    @Override
    protected @NonNull String[] getTimingPath() {
        return new String[] { "response", "time" };
    }
}