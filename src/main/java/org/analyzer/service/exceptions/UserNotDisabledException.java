package org.analyzer.service.exceptions;

import lombok.NonNull;

public class UserNotDisabledException extends RuntimeException {

    public UserNotDisabledException(@NonNull final String username) {
        super(username);
    }
}
