package org.analyzer.service.exceptions;

import lombok.NonNull;

public class UserAlreadyDisabledException extends RuntimeException {

    public UserAlreadyDisabledException(@NonNull final String username) {
        super(username);
    }
}
