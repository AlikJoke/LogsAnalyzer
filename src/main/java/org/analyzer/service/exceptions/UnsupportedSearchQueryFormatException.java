package org.analyzer.service.exceptions;

import org.analyzer.i18n.MessageHelper;

public class UnsupportedSearchQueryFormatException extends RuntimeException {

    public UnsupportedSearchQueryFormatException() {
        super(MessageHelper.getMessage("org.analyzer.unsupported.query.format"));
    }
}
