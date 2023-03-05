package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class NotEnoughOperationParametersException extends RuntimeException {

    public NotEnoughOperationParametersException(@NonNull String operation, int argsExpected, int argsActual) {
        super(MessageHelper.getMessage("org.analyzer.operation.not.enough.params", operation, argsExpected, argsActual));
    }
}
