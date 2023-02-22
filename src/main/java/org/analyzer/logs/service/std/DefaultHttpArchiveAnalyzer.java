package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveBody;
import org.analyzer.logs.service.HttpArchiveAggregator;
import org.analyzer.logs.service.HttpArchiveAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultHttpArchiveAnalyzer implements HttpArchiveAnalyzer {

    @Autowired
    private List<HttpArchiveAggregator<?>> aggregators;

    @NonNull
    @Override
    public Map<String, Object> analyze(@NonNull HttpArchiveBody httpArchiveBody) {

        final Map<String, Object> stats = new HashMap<>(aggregators.size(), 1);
        return this.aggregators.stream()
                                .collect(
                                        Collectors.toMap(
                                                HttpArchiveAggregator::getName,
                                                aggregator -> aggregator.apply(httpArchiveBody)
                                        )
                                );
    }
}
