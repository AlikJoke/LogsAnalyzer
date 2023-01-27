package org.analyzer.logs.service.std;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.LogKeysFactory;
import org.analyzer.logs.service.LogRecordFormat;
import org.analyzer.logs.service.LogRecordsParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;

@Component
public class DefaultLogRecordsParser implements LogRecordsParser {

    /**
     * Pattern for format like '2023-01-01 10:00:02,213 INFO  [org.example.SomeClass1] (thread-1) Any text'
     */
    private static final Pattern defaultRecordPattern =
            Pattern.compile("^(((?<date>\\d{4}-\\d{2}-\\d{2})\\s)?(?<time>\\d{2}:\\d{2}:\\d{2}[0-9,.]*?))\\s*(?<level>[A-Za-z]*)\\s*\\[(?<category>[A-Za-z0-9._]*)]\\s*\\((?<thread>[A-Za-z0-9.,\\-_\\s]*)\\)\\s*(?<text>[\\S\\s]*)(\\n?)$");

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
                    .appendLiteral(',')
                    .appendValue(MILLI_OF_SECOND, 3)
                    .toFormatter();

    private final Map<String, Pattern> patternsCache;
    private final Map<String, DateTimeFormatter> dateTimeFormattersCache;

    private final LogKeysFactory logKeysFactory;

    @Autowired
    public DefaultLogRecordsParser(
            @NonNull MeterRegistry meterRegistry,
            @NonNull LogKeysFactory logKeysFactory) {
        this.logKeysFactory = logKeysFactory;
        this.patternsCache = new ConcurrentHashMap<>();
        this.dateTimeFormattersCache = new ConcurrentHashMap<>();

        meterRegistry.gaugeMapSize(
                "logs.parser.records.patterns",
                Collections.singleton(Tag.of("description", "Number of used custom record patterns")),
                this.patternsCache);

        meterRegistry.gaugeMapSize(
                "logs.parser.records.date-time.formatters",
                Collections.singleton(Tag.of("description", "Number of used custom date-time formatters for timestamp of records")),
                this.dateTimeFormattersCache);
    }

    @Nonnull
    @Override
    public Flux<LogRecordEntity> parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat) {
        final var pattern = recordFormat == null || !StringUtils.hasLength(recordFormat.pattern())
                                    ? defaultRecordPattern
                                    : this.patternsCache.computeIfAbsent(recordFormat.pattern(), Pattern::compile);
        final var dateFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.dateFormat())
                                                    ? defaultDateFormatter
                                                    : computeFormatter(recordFormat.dateFormat());
        final var timeFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.timeFormat())
                                                    ? defaultTimeFormatter
                                                    : computeFormatter(recordFormat.timeFormat());

        final ThreadLocal<LogRecordEntity> lastRecord = new ThreadLocal<>();

        return readAllLines(logFile.toPath())
                .index()
                .map(tuple -> {

                    final var counter = tuple.getT1();
                    final var line = tuple.getT2();
                    final var matcher = pattern.matcher(line);
                    final var lastRecordLocal = lastRecord.get();
                    if (matcher.matches()) {
                        return LogRecordEntity
                                    .builder()
                                        .id(this.logKeysFactory.createLogRecordKey(logKey, counter))
                                        .time(parseTime(timeFormatter, matcher))
                                        .date(parseDate(dateFormatter, matcher))
                                        .level(matcher.group("level"))
                                        .thread(matcher.group("thread"))
                                        .category(matcher.group("category"))
                                        .source(line)
                                        .record(matcher.group("text"))
                                    .build();
                    } else if (lastRecordLocal != null) {
                        final var separatedLine = System.lineSeparator() + line;
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
        final var time = matcher.group("time");
        if (!StringUtils.hasLength(time)) {
            return null;
        }

        return LocalTime.from(timeFormatter.parse(time));
    }

    private LocalDate parseDate(final DateTimeFormatter dateFormatter, final Matcher matcher) {
        final var date = matcher.group("date");
        if (!StringUtils.hasLength(date)) {
            return null;
        }

        return LocalDate.from(dateFormatter.parse(date));
    }
}
