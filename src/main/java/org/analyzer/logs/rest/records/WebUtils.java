package org.analyzer.logs.rest.records;

import lombok.NonNull;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Component
public class WebUtils {

    @NonNull
    Mono<File> createTempFile(@NonNull final FilePart filePart) {
        try {
            final var destDirPath = Files.createTempDirectory(UUID.randomUUID().toString());
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            final var result = destDirPath.resolve(filePart.filename());
            return filePart.transferTo(result).thenReturn(result.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
