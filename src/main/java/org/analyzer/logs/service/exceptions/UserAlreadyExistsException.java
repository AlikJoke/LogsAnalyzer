package org.analyzer.logs.service.exceptions;

import lombok.NonNull;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(@NonNull final String username) {
        super(username);
    }
}
