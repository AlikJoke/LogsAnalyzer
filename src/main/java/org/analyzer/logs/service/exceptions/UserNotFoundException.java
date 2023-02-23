package org.analyzer.logs.service.exceptions;

import lombok.NonNull;

public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(@NonNull String username) {
        super(username);
    }
}
