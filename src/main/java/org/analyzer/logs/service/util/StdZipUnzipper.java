package org.analyzer.logs.service.util;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

@Component
public class StdZipUnzipper extends Unzipper {

    @Override
    protected boolean supported(int markerByte) {
        return markerByte == 0x504B0304 || markerByte == 0x504B0506 || markerByte == 0x504B0708;
    }

    @Override
    @NonNull
    protected List<File> unzip(@NonNull File file, @NonNull File targetDir) throws IOException {

        final List<File> result = new ArrayList<>();

        try (final var fis = new FileInputStream(file);
             final var zipIn = new ZipInputStream(fis)) {

            var entry = zipIn.getNextEntry();

            while (entry != null) {
                final var filePath = targetDir.getAbsolutePath() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    createParentDirsIfNeed(entry.getName(), targetDir);
                    extractFile(zipIn, filePath);

                    result.add(new File(filePath));
                } else {
                    var dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        return result;
    }
}
