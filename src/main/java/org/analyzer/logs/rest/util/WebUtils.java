package org.analyzer.logs.rest.util;

import lombok.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
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

    @NonNull
    public <T> ResponseEntity<T> prepareResponse(@Nullable final String exportToFilename, @NonNull final T value) throws IOException {
        if (exportToFilename == null) {
            return ResponseEntity.ok(value);
        }

        final var header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportToFilename);
        header.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        header.add(HttpHeaders.PRAGMA, "no-cache");
        header.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(value instanceof Resource ? ((Resource) value).contentLength() : 0)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(value);
    }
}
