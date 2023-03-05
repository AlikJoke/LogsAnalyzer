package org.analyzer.service.exceptions;

import org.analyzer.i18n.MessageHelper;

public final class UserNotDefinedException extends RuntimeException {

    public UserNotDefinedException() {
        super(MessageHelper.getMessage("org.analyzer.user.not.defined"));
    }
}
