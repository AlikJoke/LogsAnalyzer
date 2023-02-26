package org.analyzer.service.logs;

import javax.annotation.Nullable;

public interface LogRecordFormat {

    @Nullable
    String pattern();

    @Nullable
    String timeFormat();

    @Nullable
    String dateFormat();
}
