package org.analyzer.logs.service.exceptions;

import lombok.NonNull;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(@NonNull String username) {
        super(username);
    }
}
