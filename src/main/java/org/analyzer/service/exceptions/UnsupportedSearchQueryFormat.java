package org.analyzer.service.exceptions;

import lombok.NonNull;

public class UnsupportedSearchQueryFormat extends RuntimeException {

    public UnsupportedSearchQueryFormat(@NonNull final String message) {
        super(message);
    }
}
