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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public LogRecordsPackageIterator parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat) throws IOException {
        final var pattern = recordFormat == null || !StringUtils.hasLength(recordFormat.pattern())
                                    ? defaultRecordPattern
                                    : this.patternsCache.computeIfAbsent(recordFormat.pattern(), Pattern::compile);
        final var dateFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.dateFormat())
                                                    ? defaultDateFormatter
                                                    : computeFormatter(recordFormat.dateFormat());
        final var timeFormatter = recordFormat == null || !StringUtils.hasLength(recordFormat.timeFormat())
                                                    ? defaultTimeFormatter
                                                    : computeFormatter(recordFormat.timeFormat());

        return new LazyLogRecordsPackageIterator(logFile, logKey, pattern, dateFormatter, timeFormatter);
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

    private class LazyLogRecordsPackageIterator implements LogRecordsPackageIterator {

        private static final int PACKAGE_SIZE = 1_000;

        private final String logKey;
        private final Pattern pattern;
        private final DateTimeFormatter dateFormatter;
        private final DateTimeFormatter timeFormatter;
        private final BufferedReader reader;

        private String lastLine;
        private long offset = 0;

        private LazyLogRecordsPackageIterator(
                final File file,
                final String logKey,
                final Pattern pattern,
                final DateTimeFormatter dateFormatter,
                final DateTimeFormatter timeFormatter) throws IOException {
            this.logKey = logKey;
            this.reader = new BufferedReader(new FileReader(file));
            this.lastLine = readNextLine();
            this.pattern = pattern;
            this.dateFormatter = dateFormatter;
            this.timeFormatter = timeFormatter;
        }

        @Override
        public boolean hasNext() {
            return this.lastLine != null;
        }

        @Override
        public List<LogRecordEntity> next() {
            if (this.lastLine == null) {
                throw new NoSuchElementException();
            }

            final List<LogRecordEntity> result = new ArrayList<>(PACKAGE_SIZE);

            LogRecordEntity lastRecord = null;
            do {
                final var matcher = this.pattern.matcher(this.lastLine);
                if (matcher.matches()) {

                    final long counter = this.offset + 1;
                    if (counter % PACKAGE_SIZE == 0) {
                        break;
                    }

                    lastRecord = LogRecordEntity
                                        .builder()
                                            .id(logKeysFactory.createLogRecordKey(this.logKey, ++this.offset))
                                            .time(parseTime(this.timeFormatter, matcher))
                                            .date(parseDate(this.dateFormatter, matcher))
                                            .level(matcher.group("level"))
                                            .thread(matcher.group("thread"))
                                            .category(matcher.group("category"))
                                            .source(this.lastLine)
                                            .record(matcher.group("text"))
                                        .build();
                    result.add(lastRecord);
                } else if (lastRecord != null) {
                    final var separatedLine = System.lineSeparator() + this.lastLine;
                    lastRecord.setSource(lastRecord.getSource() + separatedLine);
                    lastRecord.setRecord(lastRecord.getRecord() + separatedLine);
                } else {
                    throw new RuntimeException("Unsupported log record format: " + this.lastLine);
                }

                this.lastLine = readNextLine();
            } while ((this.lastLine = readNextLine()) != null);

            return result;
        }

        @Override
        public void close() {
            try {
                this.reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String readNextLine() {
            try {
                return this.reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
