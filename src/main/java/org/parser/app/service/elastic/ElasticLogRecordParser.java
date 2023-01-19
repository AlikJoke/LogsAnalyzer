package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.model.LogRecord;
import org.parser.app.service.LogRecordFormat;
import org.parser.app.service.LogRecordParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;

@Component
public class ElasticLogRecordParser implements LogRecordParser {

    private final Map<String, Pattern> patternsCache = new ConcurrentHashMap<>();
    private final Map<String, DateTimeFormatter> dateTimeFormattersCache = new ConcurrentHashMap<>();

    /**
     * Pattern for format like '2023-01-01 10:00:02,213 INFO  [org.example.SomeClass1] (thread-1) Any text'
     */
    private static final Pattern defaultRecordPattern =
            Pattern.compile("^(?<timestamp>(\\d{4}-\\d{2}-\\d{2}\\s)?\\d{2}:\\d{2}:\\d{2}[0-9,.]*?)\\s*(?<level>[A-Za-z]*)\\s*\\[(?<category>[A-Za-z0-9._]*)]\\s*\\((?<thread>[A-Za-z0-9.,\\-_\\s]*)\\)\\s*(?<text>[\\S\\s]*)(\\n?)$");

    private static final DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter defaultTimeFormatter
            = new DateTimeFormatterBuilder()
                    .appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2)
                    .optionalStart()
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .optionalStart()
                    .appendFraction(MILLI_OF_SECOND, 0, 3, true)
                    .toFormatter();

    @Nonnull
    @Override
    public Flux<LogRecord> parse(
            @Nonnull Mono<File> logFile,
            @NonNull String fileName,
            @Nullable LogRecordFormat recordFormat) {
        final Pattern pattern = recordFormat == null || !StringUtils.hasLength(recordFormat.pattern())
                                    ? defaultRecordPattern
                                    : this.patternsCache.computeIfAbsent(recordFormat.pattern(), Pattern::compile);
        final DateTimeFormatter dateFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.dateFormat())
                                                    ? defaultDateFormatter
                                                    : computeFormatter(recordFormat.dateFormat());
        final DateTimeFormatter timeFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.timeFormat())
                                                    ? defaultTimeFormatter
                                                    : computeFormatter(recordFormat.timeFormat());

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
                                        .time(parseTime(timeFormatter, matcher))
                                        .date(parseDate(dateFormatter, matcher))
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

    private DateTimeFormatter computeFormatter(final String format) {
        return this.dateTimeFormattersCache
                        .computeIfAbsent(
                                format,
                                formatKey -> new DateTimeFormatterBuilder()
                                                    .appendPattern(formatKey)
                                                    .toFormatter()
                        );
    }

    private Flux<String> readAllLines(final Path path) {
        return Flux.using(
                () -> Files.lines(path),
                Flux::fromStream,
                Stream::close
        );
    }

    private LocalTime parseTime(final DateTimeFormatter timeFormatter, final Matcher matcher) {
        final String time = matcher.group("time");
        if (StringUtils.hasLength(time)) {
            return null;
        }

        return LocalTime.from(timeFormatter.parse(time));
    }

    private LocalDate parseDate(final DateTimeFormatter dateFormatter, final Matcher matcher) {
        final String date = matcher.group("date");
        if (StringUtils.hasLength(date)) {
            return null;
        }

        return LocalDate.from(dateFormatter.parse(date));
    }
}
