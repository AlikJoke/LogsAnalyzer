package org.analyzer.logs.service.util;

import lombok.NonNull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@Component
public class GZipTarUnzipper extends Unzipper {

    @Override
    protected boolean supported(int markerByte) {
        return markerByte == 529205248;
    }

    @Override
    @NonNull
    protected List<File> unzip(@NonNull File file, @NonNull File targetDir) throws IOException {

        final var tempTarFile = Files.createTempFile(targetDir.toPath(), UUID.randomUUID().toString(), "").toFile();
        try (final var fis = new FileInputStream(file);
             final var gZIPInputStream = new GZIPInputStream(fis);
             final var fos = new FileOutputStream(tempTarFile)) {

            final var buffer = new byte[1024];
            var len = -1;
            while ((len = gZIPInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        final List<File> result = new ArrayList<>();

        try (final var fis = new FileInputStream(tempTarFile);
             final var tis = new TarArchiveInputStream(fis)) {
            TarArchiveEntry tarEntry;

            while ((tarEntry = tis.getNextTarEntry()) != null) {
                final var filePath = targetDir.getAbsolutePath() + File.separator + tarEntry.getName();
                if (tarEntry.isDirectory()) {
                    var dir = new File(filePath);
                    dir.mkdirs();
                } else {
                    createParentDirsIfNeed(tarEntry.getName(), targetDir);

                    final File extractedFile = new File(filePath);
                    try (final var fos = new FileOutputStream(extractedFile)) {
                        IOUtils.copy(tis, fos);
                    }

                    result.add(extractedFile);
                }
            }
        } finally {
            tempTarFile.delete();
        }

        return result;
    }
}
