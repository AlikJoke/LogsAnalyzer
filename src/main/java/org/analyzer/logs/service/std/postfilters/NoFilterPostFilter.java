package org.analyzer.logs.service.std.postfilters;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.PostFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(NoFilterPostFilter.NAME)
class NoFilterPostFilter implements PostFilter {

    static final String NAME = "no-filter";

    @NonNull
    @Override
    public List<LogRecordEntity> apply(@NonNull List<LogRecordEntity> records) {
        return records;
    }

    @NonNull
    @Override
    public Object getParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParameters(@NonNull Object parameters) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Class<?> getParametersClass() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }
}
