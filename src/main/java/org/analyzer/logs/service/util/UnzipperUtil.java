package org.analyzer.logs.service.util;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.function.Predicate;

@Component
public class UnzipperUtil {

    @Autowired
    private List<Unzipper> unzippers;

    @NonNull
    public Flux<File> flat(@NonNull final File file) {
        return Flux.just(file)
                    .filter(Predicate.not(this::isArchive))
                    .switchIfEmpty(Flux.defer(() -> flatArchive(file).cache()));
    }

    private Flux<File> flatArchive(@NonNull final File file) {
        try {
            try (final var raf = new RandomAccessFile(file, "r")) {
                final var fileSignature = raf.readInt();

                return this.unzippers.stream()
                                        .filter(unzipper -> unzipper.supported(fileSignature))
                                        .findAny()
                                        .map(unzipper -> unzipper.unzip(file))
                                        .orElseThrow()
                                        .flatMap(this::flat);
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
