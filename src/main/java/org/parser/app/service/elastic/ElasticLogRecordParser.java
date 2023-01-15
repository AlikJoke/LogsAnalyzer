package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.LogRecordParser;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ElasticLogRecordParser implements LogRecordParser {

    private final Map<String, Pattern> patternsCache = new ConcurrentHashMap<>();

    /**
     * Pattern for format like '2023-01-01 10:00:02,213 INFO  [org.example.SomeClass1] (thread-1) Any text'
     */
    private static final Pattern defaultRecordPattern =
            Pattern.compile("^(?<timestamp>\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}[0-9,.]*?)\\s*(?<level>[A-Za-z]*)\\s*\\[(?<category>[A-Za-z0-9._]*)]\\s*\\((?<thread>[A-Za-z0-9.,\\-_]*)\\)\\s*(?<text>[\\S\\s]*)(\\n?)$");

    @Nonnull
    @Override
    public List<LogRecord> parse(@Nonnull File logFile, @NonNull String fileName, @Nullable String recordFormat) {
        final Pattern pattern = recordFormat == null ? defaultRecordPattern : this.patternsCache.computeIfAbsent(recordFormat, Pattern::compile);

        final List<LogRecord> result = new ArrayList<>();

        // TODO support for another timestamp formats
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        StringBuilder additionalRecordPartsBuilder = null;
        LogRecord record = null;
        long idx = 0;
        for (final var line : readAllLinesFromFile(logFile)) {
            final var matcher = pattern.matcher(line);
            if (matcher.matches()) {
                if (record != null && additionalRecordPartsBuilder != null) {
                    final String additionalRecordString = additionalRecordPartsBuilder.toString();
                    record.setSource(record.getSource() + additionalRecordString);
                    record.setRecord(record.getRecord() + additionalRecordString);

                    additionalRecordPartsBuilder = null;
                }

                record = LogRecord
                            .builder()
                                .id(fileName + "@" + ++idx)
                                .timestamp(parseTimestamp(sdf, matcher))
                                .level(matcher.group("level"))
                                .thread(matcher.group("thread"))
                                .category(matcher.group("category"))
                                .source(line)
                                .record(matcher.group("text"))
                            .build();
                result.add(record);
            } else if (record != null) {
                additionalRecordPartsBuilder = additionalRecordPartsBuilder == null ? new StringBuilder(System.lineSeparator() + line) : additionalRecordPartsBuilder.append(System.lineSeparator() + line);
            }
        }

        return result;
    }

    private List<String> readAllLinesFromFile(final File logFile) {
        try {
            return Files.readAllLines(logFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime parseTimestamp(final SimpleDateFormat sdf, final Matcher matcher) {
        try {
            final String timestampString = matcher.group("timestamp").replace(',', '.');
            final Date timestamp = sdf.parse(timestampString);
            return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
