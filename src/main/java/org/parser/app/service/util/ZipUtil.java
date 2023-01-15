package org.parser.app.service.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipUtil {

    public boolean isArchive(@NonNull File file) {
        try (final var raf = new RandomAccessFile(file, "r")) {
            final var fileSignature = raf.readInt();
            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        } catch (IOException e) {
        }

        return false;
    }

    @NonNull
    public List<File> unzip(@NonNull final File zip) throws IOException {

        final var destDir = Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
        destDir.deleteOnExit();

        if (!destDir.exists()) {
            throw new FileNotFoundException("Can not create dir: " + destDir.getAbsolutePath());
        }

        try (final InputStream fis = new FileInputStream(zip);
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

        return destDir.listFiles() == null ? Collections.emptyList() : Arrays.asList(destDir.listFiles());
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
