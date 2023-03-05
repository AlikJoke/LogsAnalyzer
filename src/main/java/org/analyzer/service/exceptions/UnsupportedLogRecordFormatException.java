package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UnsupportedLogRecordFormatException extends RuntimeException {

    public UnsupportedLogRecordFormatException(@NonNull String record) {
        super(MessageHelper.getMessage("org.analyzer.unsupported.log.record.format", record));
    }
}
