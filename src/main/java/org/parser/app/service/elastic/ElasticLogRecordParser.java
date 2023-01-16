package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.LogRecordParser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    public Flux<LogRecord> parse(
            @Nonnull Mono<File> logFile,
            @NonNull String fileName,
            @Nullable String recordFormat) {
        final Pattern pattern = recordFormat == null ? defaultRecordPattern : this.patternsCache.computeIfAbsent(recordFormat, Pattern::compile);

        // TODO support for another timestamp formats
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        final ThreadLocal<LogRecord> lastRecord = new ThreadLocal<>();

        return logFile
                .map(File::toPath)
                .flatMapMany(this::readAllLines)
                .index()
                .map(line2idx -> {

                    final var line = line2idx.getT2();
                    final var matcher = pattern.matcher(line);
                    final var lastRecordLocal = lastRecord.get();
                    if (matcher.matches()) {
                        return LogRecord
                                    .builder()
                                        .id(fileName + "@" + line2idx.getT1())
                                        .timestamp(parseTimestamp(sdf, matcher))
                                        .level(matcher.group("level"))
                                        .thread(matcher.group("thread"))
                                        .category(matcher.group("category"))
                                        .source(line2idx.getT2())
                                        .record(matcher.group("text"))
                                    .build();
                    } else if (lastRecordLocal != null) {
                        final String separatedLine = System.lineSeparator() + line;
                        lastRecordLocal.setSource(lastRecordLocal.getSource() + separatedLine);
                        lastRecordLocal.setRecord(lastRecordLocal.getRecord() + separatedLine);

                        return lastRecordLocal;
                    }

                    return null;
                })
                .doOnNext(lastRecord::set)
                .distinctUntilChanged();
    }

    private Flux<String> readAllLines(final Path path) {
        return Flux.using(
                () -> Files.lines(path),
                Flux::fromStream,
                Stream::close
        );
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
