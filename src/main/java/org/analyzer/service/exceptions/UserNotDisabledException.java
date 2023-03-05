package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UserNotDisabledException extends RuntimeException {

    public UserNotDisabledException(@NonNull final String username) {
        super(MessageHelper.getMessage("org.analyzer.user.not.disabled", username));
    }
}
