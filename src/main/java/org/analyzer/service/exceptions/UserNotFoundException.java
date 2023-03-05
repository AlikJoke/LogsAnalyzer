package org.analyzer.service.exceptions;

import lombok.NonNull;
import org.analyzer.i18n.MessageHelper;

public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(@NonNull String username) {
        super(MessageHelper.getMessage("org.analyzer.user.not.found", username));
    }
}
