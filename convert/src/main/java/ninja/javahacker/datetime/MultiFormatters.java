package ninja.javahacker.datetime;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public enum MultiFormatters {
    YMD_DASH,
    DMY_DASH,
    MDY_DASH,
    YMD_SLASH,
    DMY_SLASH,
    MDY_SLASH,
    YMD_DOT,
    DMY_DOT,
    MDY_DOT,
    YMD_JOINED,
    DMY_JOINED,
    MDY_JOINED,
    ISO_8601;

    @NonNull
    private final Pattern patternDTZ;

    @NonNull
    private final Pattern patternDT;

    @NonNull
    private final Pattern patternTZ;

    @NonNull
    private final Pattern patternD;

    @NonNull
    private final Pattern patternT;

    @NonNull
    private final DateTimeFormatter formatDTZ;

    @NonNull
    private final DateTimeFormatter formatDT;

    @NonNull
    private final DateTimeFormatter formatTZ;

    @NonNull
    private final DateTimeFormatter formatD;

    @NonNull
    private final DateTimeFormatter formatT;

    {
        var y = "[0-9]{4}";
        var md = "[0-9]{2}";
        var sepr = name().contains("JOINED") ? "" : name().contains("DOT") ? "\\." : name().contains("SLASH") ? "/" : "\\-";
        var sepf = sepr.replace("\\", "");
        var dt = name().charAt(2) == 'y' ? md + sepr + md + sepr + y : y + sepr + md + sepr + md;
        var hh = "[0-9]{2}\\:[0-9]{2}(?:\\:[0-9]{2}(?:\\.[0-9]{1-9}))";
        var tz = "(?:\\+\\-)[0-9]{2}\\:[0-9]{2}(?:\\:[0-9]{2})";
        var x1 = name().contains("ISO") ? "T" : " ";
        var x2 = name().contains("ISO") ? "" : " ";
        patternDTZ = Pattern.compile("^" + dt + x1 + hh + x2 + tz + "$");
        patternDT = Pattern.compile("^" + dt + x1 + hh + "$");
        patternTZ = Pattern.compile("^" + hh + x2 + tz + "$");
        patternD = Pattern.compile("^" + dt + "$");
        patternT = Pattern.compile("^" + hh + "$");
        if (name().contains("ISO")) {
            var fd = name().contains("YMD") ? "uuuu-MM-dd" : name().contains("DMY") ? "dd-MM-yyyy" : "MM-dd-yyyy";
            fd = fd.replace("-", sepf);
            var fdtz = fd + " HH:mm:ss.SSSSSSSSS xxxxx";
            var fdt = fd + " HH:mm:ss.SSSSSSSSS";
            var ftz = "HH:mm:ss.SSSSSSSSS xxxxx";
            var ft = "HH:mm:ss.SSSSSSSSS";
            formatDTZ = DateTimeFormatter.ofPattern(fdtz).withResolverStyle(ResolverStyle.STRICT);
            formatDT = DateTimeFormatter.ofPattern(fdt).withResolverStyle(ResolverStyle.STRICT);
            formatD = DateTimeFormatter.ofPattern(fd).withResolverStyle(ResolverStyle.STRICT);
            formatTZ = DateTimeFormatter.ofPattern(ftz).withResolverStyle(ResolverStyle.STRICT);
            formatT = DateTimeFormatter.ofPattern(ft).withResolverStyle(ResolverStyle.STRICT);
        } else {
            formatDTZ = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            formatDT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            formatD = DateTimeFormatter.ISO_LOCAL_DATE;
            formatTZ = DateTimeFormatter.ISO_OFFSET_TIME;
            formatT = DateTimeFormatter.ISO_LOCAL_TIME;
        }
    }

    @NonNull
    private static String fillUpToNanoSeconds(@NonNull String x) {
        var complement = "00:00:00.000000000";
        return x + complement.substring(x.length());
    }

    @NonNull
    private <E> E parse(
            @NonNull String s,
            @NonNull BiFunction<CharSequence, DateTimeFormatter, E> func,
            boolean mustHaveDate)
            throws DateTimeParseException
    {
        checkNotNull(s);
        checkNotNull(func);

        var dtz = patternDTZ.asPredicate().test(s);
        var dt = patternDT.asPredicate().test(s);
        var d = patternD.asPredicate().test(s);
        var tz = patternTZ.asPredicate().test(s);
        var t = patternT.asPredicate().test(s);

        var ttt = List.of(dtz, dtz, d, tz, t).stream().filter(x -> x).count();
        assertOrder(ttt, 1L);

        var parts = s.split(" ");
        if (dtz || dt) {
            parts[1] = fillUpToNanoSeconds(parts[1]);
        } else if (t || tz) {
            parts[0] = fillUpToNanoSeconds(parts[0]);
        }

        var fmt = dt ? formatDT : d ? formatD : (tz && !mustHaveDate) ? formatTZ : (tz && !mustHaveDate) ? formatT : formatDTZ;

        return func.apply(s, fmt); // throws DateTimeParseException
    }

    @NonNull
    public Instant parseInstant(@NonNull String s) throws DateTimeParseException {
        try {
            return parse(s, (x, d) -> OffsetDateTime.parse(x, d).toInstant(), true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDateTime(s).atOffset(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public OffsetDateTime parseOffsetDateTime(@NonNull String s) throws DateTimeParseException {
        try {
            return parse(s, OffsetDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDateTime(s).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public ZonedDateTime parseZonedDateTime(@NonNull String s) throws DateTimeParseException {
        try {
            return parse(s, ZonedDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDateTime(s).atZone(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public LocalDateTime parseLocalDateTime(@NonNull String s) throws DateTimeParseException {
        try {
            return parse(s, LocalDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDate(s).atTime(LocalTime.MIN);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public LocalDate parseLocalDate(@NonNull String s) throws DateTimeParseException {
        return parse(s, LocalDate::parse, true);
    }

    @NonNull
    public OffsetTime parseOffsetTime(@NonNull String s) throws DateTimeParseException {
        try {
            return parse(s, OffsetTime::parse, false);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalTime(s).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    @NonNull
    public LocalTime parseLocalTime(@NonNull String s) throws DateTimeParseException {
        return parse(s, LocalTime::parse, false);
    }

    @NonNull
    private String removeExcessZeros(@NonNull String input) {
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
    public String format(@NonNull Instant s) {
        return format(LocalDateTime.ofInstant(s, ZoneOffset.UTC));
    }

    @NonNull
    public String format(@NonNull OffsetDateTime s) {
        return removeExcessZeros(formatDTZ.format(s));
    }

    @NonNull
    public String format(@NonNull ZonedDateTime s) {
        return removeExcessZeros(formatDTZ.format(s));
    }

    @NonNull
    public String format(@NonNull LocalDateTime s) {
        return removeExcessZeros(formatDT.format(s));
    }

    @NonNull
    public String format(@NonNull LocalDate s) {
        return formatD.format(s);
    }

    @NonNull
    public String format(@NonNull OffsetTime s) {
        return removeExcessZeros(formatTZ.format(s));
    }

    @NonNull
    public String format(@NonNull LocalTime s) {
        return removeExcessZeros(formatT.format(s));
    }

    @Generated
    private static void assertOrder(long a, long b) {
        if (a > b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}