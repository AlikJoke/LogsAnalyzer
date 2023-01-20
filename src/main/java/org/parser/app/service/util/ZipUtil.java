package org.parser.app.service.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipUtil {

    @NonNull
    public Flux<File> flat(@NonNull final Mono<File> zip) {
        return zip
                .filter(Predicate.not(this::isArchive))
                .flux()
                .switchIfEmpty(zip.flatMapIterable(this::flatArchive));
    }

    private List<File> flatArchive(@NonNull final File zip) {
        try {
            final var destDirPath = Files.createTempDirectory(UUID.randomUUID().toString());
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            if (!destDir.exists()) {
                throw new FileNotFoundException("Can not create dir: " + destDir.getAbsolutePath());
            }

            try (final var fis = new FileInputStream(zip);
                 final var zipIn = new ZipInputStream(fis)) {
                var entry = zipIn.getNextEntry();

                while (entry != null) {
                    final var filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
                    if (!entry.isDirectory()) {
                        createParentDirsIfNeed(entry, destDir);
                        extractFile(zipIn, filePath);
                    } else {
                        var dir = new File(filePath);
                        dir.mkdirs();
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }

            final File[] childFiles = destDir.listFiles();
            return childFiles == null
                    ? Collections.emptyList()
                    : Arrays.asList(childFiles);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isArchive(@NonNull File file) {
        try (final var raf = new RandomAccessFile(file, "r")) {
            final var fileSignature = raf.readInt();
            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        } catch (IOException ignored) {
        }

        return false;
    }

    private void createParentDirsIfNeed(final ZipEntry entry, final File destDir) {

        final var separator = entry.getName().contains(File.separator) ? File.separator : "/";
        if (!entry.getName().contains(separator)) {
            return;
        }

        final var parentDirPath = entry.getName().substring(0, entry.getName().lastIndexOf(separator));
        final var parentDir = new File(destDir, parentDirPath);
        parentDir.deleteOnExit();

        parentDir.mkdirs();
    }

    private void extractFile(final ZipInputStream zipIn, final String filePath) throws IOException {

        try (final var bos = new BufferedOutputStream(new FileOutputStream(filePath))) {

            final byte[] bytesIn = new byte[1024];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
