package org.analyzer.service.har.std.aggregations;

import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TopByWaitExecutionTimesHttpArchiveAggregator extends TopByExecutionTimesHttpArchiveAggregator {
    @NonNull
    @Override
    public String getName() {
        return "requests-top-by-wait-execution-times";
    }

    @Override
    protected @NonNull String[] getTimingPath() {
        return new String[] { "response", "timings", "wait" };
    }
}