package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UserAlreadyDisabledException extends RuntimeException {

    public UserAlreadyDisabledException(@NonNull final String username) {
        super(MessageHelper.getMessage("org.analyzer.user.already.disabled", username));
    }
}
