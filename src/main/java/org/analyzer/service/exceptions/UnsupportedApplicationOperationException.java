package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UnsupportedApplicationOperationException extends UnsupportedOperationException {

    public UnsupportedApplicationOperationException(@NonNull String operation) {
        super(MessageHelper.getMessage("org.analyzer.unsupported.operation", operation));
    }
}
