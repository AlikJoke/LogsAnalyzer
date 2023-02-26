package org.analyzer.service.util;

import lombok.NonNull;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

abstract class Unzipper {

    protected abstract boolean supported(int markerByte);

    @NonNull
    protected abstract List<File> unzip(@NonNull File archive, @NonNull File targetDir) throws IOException;

    @NonNull
    protected final List<File> unzip(@NonNull File file) {

        try {
            final var destDirPath = Files.createTempDirectory(UUID.randomUUID().toString());
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            if (!destDir.exists()) {
                throw new FileNotFoundException("Can not create dir: " + destDir.getAbsolutePath());
            }

            return unzip(file, destDir);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void createParentDirsIfNeed(final String entryName, final File destDir) {

        final var separator = entryName.contains(File.separator) ? File.separator : "/";
        if (!entryName.contains(separator)) {
            return;
        }

        final var parentDirPath = entryName.substring(0, entryName.lastIndexOf(separator));
        final var parentDir = new File(destDir, parentDirPath);
        parentDir.deleteOnExit();

        parentDir.mkdirs();
    }

    protected void extractFile(final InputStream in, final String filePath) throws IOException {

        try (final var bos = new BufferedOutputStream(new FileOutputStream(filePath))) {

            final var bytesIn = new byte[1024];
            int read;
            while ((read = in.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
