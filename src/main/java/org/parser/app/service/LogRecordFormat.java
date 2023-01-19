package org.parser.app.service;

import javax.annotation.Nonnull;

public interface LogRecordFormat {

    @Nonnull
    String pattern();

    @Nonnull
    String timeFormat();

    @Nonnull
    String dateFormat();
}
