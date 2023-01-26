package org.analyzer.logs.rest.records;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Component
class WebUtils {

    Mono<File> createTempFile(final FilePart filePart) {
        try {
            final var destDirPath = Files.createTempDirectory(UUID.randomUUID().toString());
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            final var result = destDirPath.resolveSibling(filePart.filename());
            return filePart.transferTo(result).thenReturn(result.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
