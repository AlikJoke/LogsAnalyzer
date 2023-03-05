package org.analyzer.i18n;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageHelper {

    private static MessageSource messageSource;

    @Autowired
    public MessageHelper(@NonNull MessageSource source) {
        messageSource = source;
    }

    @NonNull
    public static String getMessage(@NonNull String code, @NonNull Object... params) {
        return messageSource.getMessage(code, params, LocaleContextHolder.getLocale());
    }
}
