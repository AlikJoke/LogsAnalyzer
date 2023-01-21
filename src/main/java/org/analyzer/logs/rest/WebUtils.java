package org.analyzer.logs.rest;

import lombok.NonNull;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Component
class WebUtils {

    @Nullable
    Object prepareToResponse(@NonNull final List<?> values) {

        final Object firstElem = values.get(0);
        if (firstElem instanceof Tuple2<?,?>) {
            return values
                    .stream()
                    .filter(v -> v instanceof Tuple2<?,?>)
                    .map(Tuple2.class::cast)
                    .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
        }

        return values.size() == 1 ? firstElem : values;
    }

    Mono<File> createTempFile(final FilePart filePart) {
        try {
            final File result = Files.createTempFile(filePart.filename(), null).toFile();
            return filePart.transferTo(result).thenReturn(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
