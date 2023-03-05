package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(@NonNull final String username) {
        super(MessageHelper.getMessage("org.analyzer.user.already.exists", username));
    }
}
