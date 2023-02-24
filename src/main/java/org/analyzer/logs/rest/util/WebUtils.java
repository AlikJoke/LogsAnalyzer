package org.analyzer.logs.rest.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Component
public class WebUtils {

    @NonNull
    public File createTempFile(@NonNull final MultipartFile multipartFile) {
        try {
            final var destDirPath = Files.createTempDirectory(UUID.randomUUID().toString());
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            final var result = destDirPath.resolve(multipartFile.getOriginalFilename());
            multipartFile.transferTo(result);

            return result.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
