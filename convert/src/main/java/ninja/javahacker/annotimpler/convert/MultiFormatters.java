package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class MultiFormatters {

    private MultiFormatters() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    private static final List<DateTimeFormatter> PATTERNS_DTZ = Stream.of(
            "uuuu-MM-dd HH:mm:ss.SSSSSSSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSSSSSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSSSSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSSSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SSS xxxxx",
            "uuuu-MM-dd HH:mm:ss.SS xxxxx",
            "uuuu-MM-dd HH:mm:ss.S xxxxx",
            "uuuu-MM-dd HH:mm:ss xxxxx",
            "uuuu-MM-dd HH:mm xxxxx"
    ).map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    @NonNull
    private static final List<DateTimeFormatter> PATTERNS_DT = Stream.of(
            "uuuu-MM-dd HH:mm:ss.SSSSSSSSS",
            "uuuu-MM-dd HH:mm:ss.SSSSSSSS",
            "uuuu-MM-dd HH:mm:ss.SSSSSSS",
            "uuuu-MM-dd HH:mm:ss.SSSSSS",
            "uuuu-MM-dd HH:mm:ss.SSSSS",
            "uuuu-MM-dd HH:mm:ss.SSSS",
            "uuuu-MM-dd HH:mm:ss.SSS",
            "uuuu-MM-dd HH:mm:ss.SS",
            "uuuu-MM-dd HH:mm:ss.S",
            "uuuu-MM-dd HH:mm:ss",
            "uuuu-MM-dd HH:mm"
    ).map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    @NonNull
    private static final List<DateTimeFormatter> PATTERNS_TZ = Stream.of(
            "HH:mm:ss.SSSSSSSSS xxxxx",
            "HH:mm:ss.SSSSSSSS xxxxx",
            "HH:mm:ss.SSSSSSS xxxxx",
            "HH:mm:ss.SSSSSS xxxxx",
            "HH:mm:ss.SSSSS xxxxx",
            "HH:mm:ss.SSSS xxxxx",
            "HH:mm:ss.SSS xxxxx",
            "HH:mm:ss.SS xxxxx",
            "HH:mm:ss.S xxxxx",
            "HH:mm:ss xxxxx",
            "HH:mm xxxxx"
    ).map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    @NonNull
    private static final List<DateTimeFormatter> PATTERNS_T = Stream.of(
            "HH:mm:ss.SSSSSSSSS",
            "HH:mm:ss.SSSSSSSS",
            "HH:mm:ss.SSSSSSS",
            "HH:mm:ss.SSSSSS",
            "HH:mm:ss.SSSSS",
            "HH:mm:ss.SSSS",
            "HH:mm:ss.SSS",
            "HH:mm:ss.SS",
            "HH:mm:ss.S",
            "HH:mm:ss",
            "HH:mm"
    ).map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    @NonNull
    private static final List<DateTimeFormatter> PATTERNS_D = Stream.of(
            "uuuu-MM-dd"
    ).map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    @NonNull
    private static final DateTimeFormatter PATTERN_D = PATTERNS_D.get(0);

    @NonNull
    private static final DateTimeFormatter PATTERN_DTZ = PATTERNS_DTZ.get(0);

    @NonNull
    private static final DateTimeFormatter PATTERN_DT = PATTERNS_DT.get(0);

    @NonNull
    private static final DateTimeFormatter PATTERN_TZ = PATTERNS_TZ.get(0);

    @NonNull
    private static final DateTimeFormatter PATTERN_T = PATTERNS_T.get(0);

    @NonNull
    private static <E> E parse(
            @NonNull String s,
            @NonNull BiFunction<CharSequence, DateTimeFormatter, E> func,
            boolean mustHaveDate,
            @NonNull Class<E> out)
            throws ConvertionException
    {
        checkNotNull(s);
        checkNotNull(func);
        checkNotNull(out);
        List<DateTimeFormatter> fmts = new ArrayList<>(45);
        fmts.addAll(PATTERNS_DTZ);
        fmts.addAll(PATTERNS_DT);
        fmts.addAll(PATTERNS_D);
        if (!mustHaveDate) {
            fmts.addAll(PATTERNS_TZ);
            fmts.addAll(PATTERNS_T);
            fmts.addAll(PATTERNS_D);
        }
        var exs = new ArrayList<DateTimeParseException>(fmts.size());
        for (var fmt : fmts) {
            try {
                return func.apply(s, fmt);
            } catch (DateTimeParseException e) {
                exs.add(e);
            }
        }
        var ex = new ConvertionException(String.class, out);
        exs.forEach(e -> ex.addSuppressed(e));
        throw ex;
    }

    @NonNull
    public static Instant parseInstant(@NonNull String s) throws ConvertionException {
        try {
            return parse(s, (x, d) -> OffsetDateTime.parse(x, d).toInstant(), true, Instant.class);
        } catch (ConvertionException a) {
            try {
                return parseLocalDateTime(s).atOffset(ZoneOffset.UTC).toInstant();
            } catch (ConvertionException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public static OffsetDateTime parseOffsetDateTime(@NonNull String s) throws ConvertionException {
        try {
            return parse(s, OffsetDateTime::parse, true, OffsetDateTime.class);
        } catch (ConvertionException a) {
            try {
                return parseLocalDateTime(s).atOffset(ZoneOffset.UTC);
            } catch (ConvertionException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public static ZonedDateTime parseZonedDateTime(@NonNull String s) throws ConvertionException {
        try {
            return parse(s, ZonedDateTime::parse, true, ZonedDateTime.class);
        } catch (ConvertionException a) {
            try {
                return parseLocalDateTime(s).atZone(ZoneOffset.UTC);
            } catch (ConvertionException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public static LocalDateTime parseLocalDateTime(@NonNull String s) throws ConvertionException {
        try {
            return parse(s, LocalDateTime::parse, true, LocalDateTime.class);
        } catch (ConvertionException a) {
            try {
                return parseLocalDate(s).atTime(LocalTime.MIN);
            } catch (ConvertionException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public static LocalDate parseLocalDate(@NonNull String s) throws ConvertionException {
        return parse(s, LocalDate::parse, true, LocalDate.class);
    }

    @NonNull
    public static OffsetTime parseOffsetTime(@NonNull String s) throws ConvertionException {
        try {
            return parse(s, OffsetTime::parse, false, OffsetTime.class);
        } catch (ConvertionException a) {
            try {
                return parseLocalTime(s).atOffset(ZoneOffset.UTC);
            } catch (ConvertionException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public static LocalTime parseLocalTime(@NonNull String s) throws ConvertionException {
        return parse(s, LocalTime::parse, false, LocalTime.class);
    }

    @NonNull
    private static String removeExcessZeros(@NonNull String input) {
        checkNotNull(input);
        var parts = input.split(" ");
        for (var part : List.of(0, 1, 2)) {
            if (part >= parts.length) continue;
            var time = parts[part];
            if (!time.contains(".")) continue;
            var t = time.length();
            while (time.charAt(t - 1) == '0') {
                t--;
            }
            if (time.charAt(t - 1) == '.') {
                t--;
            }
            parts[part] = time.substring(0, t);
        }
        return String.join(" ", parts);
    }

    @NonNull
    public static String format(@NonNull Instant s) {
        return format(LocalDateTime.ofInstant(s, ZoneOffset.UTC));
    }

    @NonNull
    public static String format(@NonNull OffsetDateTime s) {
        return removeExcessZeros(PATTERN_DTZ.format(s));
    }

    @NonNull
    public static String format(@NonNull ZonedDateTime s) {
        return removeExcessZeros(PATTERN_DTZ.format(s));
    }

    @NonNull
    public static String format(@NonNull LocalDateTime s) {
        return removeExcessZeros(PATTERN_DT.format(s));
    }

    @NonNull
    public static String format(@NonNull LocalDate s) {
        return PATTERN_D.format(s);
    }

    @NonNull
    public static String format(@NonNull OffsetTime s) {
        return removeExcessZeros(PATTERN_TZ.format(s));
    }

    @NonNull
    public static String format(@NonNull LocalTime s) {
        return removeExcessZeros(PATTERN_T.format(s));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}