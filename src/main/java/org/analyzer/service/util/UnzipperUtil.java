package org.analyzer.service.util;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

@Component
public class UnzipperUtil {

    @Autowired
    private List<Unzipper> unzippers;

    @NonNull
    public List<File> flat(@NonNull final File file) {
        return isArchive(file) ? flatArchive(file) : List.of(file);
    }

    private List<File> flatArchive(@NonNull final File file) {
        try {
            try (final var raf = new RandomAccessFile(file, "r")) {
                final var fileSignature = raf.readInt();

                return this.unzippers.stream()
                                        .filter(unzipper -> unzipper.supported(fileSignature))
                                        .findAny()
                                        .map(unzipper -> unzipper.unzip(file))
                                        .orElseThrow();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isArchive(@NonNull File file) {
        try (final var raf = new RandomAccessFile(file, "r")) {
            final var fileSignature = raf.readInt();
            return this.unzippers
                            .stream()
                            .anyMatch(unzipper -> unzipper.supported(fileSignature));
        } catch (IOException ignored) {
        }

        return false;
    }
}
