package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum StringConverter implements Converter<String> {
    INSTANCE;

    @NonNull
    private static final DateTimeFormatter FORMATTER_DTZ = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS xxxxx")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    private static final DateTimeFormatter FORMATTER_DT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    private static final DateTimeFormatter FORMATTER_TZ = DateTimeFormatter
            .ofPattern("HH:mm:ss.SSSSSSSSS xxxxx")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    private static final DateTimeFormatter FORMATTER_T = DateTimeFormatter
            .ofPattern("HH:mm:ss.SSSSSSSSS")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern EXCESS_ZEROS = Pattern.compile("\\.0+ ", Pattern.CASE_INSENSITIVE);

    @NonNull
    private static String removeExcessZeros(@NonNull String input, int part) {
        checkNotNull(input);
        var parts = input.split(" ");
        var time = parts[part];
        if (!time.contains(".")) return input;
        var t = time.length();
        while (time.charAt(t - 1) == '0') {
            t--;
        }
        if (time.charAt(t - 1) == '.') {
            t--;
        }
        parts[part] = time.substring(0, t);
        return String.join(" ", parts);
    }

    @NonNull
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @NonNull
    private static final String BAD = "Can't read value as short.";

    @NonNull
    @Override
    public Optional<String> from(boolean in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(byte in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(short in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(int in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(long in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(float in) throws ConvertionException {
        if (in == Float.POSITIVE_INFINITY) return Optional.of("Infinity");
        if (in == Float.NEGATIVE_INFINITY) return Optional.of("-Infinity");
        if (Float.isNaN(in)) return Optional.of("NaN");
        return BigDecimalConverter.INSTANCE.from(in).map(bd -> bd.toPlainString());
    }

    @NonNull
    @Override
    public Optional<String> from(double in) throws ConvertionException {
        if (in == Double.POSITIVE_INFINITY) return Optional.of("Infinity");
        if (in == Double.NEGATIVE_INFINITY) return Optional.of("-Infinity");
        if (Double.isNaN(in)) return Optional.of("NaN");
        return BigDecimalConverter.INSTANCE.from(in).map(bd -> bd.toPlainString());
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull BigDecimal in) {
        return Optional.of(in.toPlainString());
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalDate in) {
        return Optional.of(in.format(LocalDateConverter.FORMATTER_D));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalTime in) {
        return Optional.of(removeExcessZeros(in.format(FORMATTER_T), 0));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalDateTime in) {
        return Optional.of(removeExcessZeros(in.format(FORMATTER_DT), 1));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetTime in) {
        return Optional.of(removeExcessZeros(in.format(FORMATTER_TZ), 0));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetDateTime in) {
        return Optional.of(removeExcessZeros(in.format(FORMATTER_DTZ), 1));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull String in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull byte[] in) {
        return Optional.of(new String(in, StandardCharsets.UTF_8));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull Blob in) throws ConvertionException {
        try {
            return Optional.of(new String(in.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(BAD, x, Blob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(BAD, x, Clob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(BAD, x, NClob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull SQLXML in) throws ConvertionException {
        try {
            return Optional.of(in.getString());
        } catch (SQLException x) {
            throw new ConvertionException(BAD, x, SQLXML.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull RowId in) {
        return Optional.of(new BigInteger(in.getBytes()).toString());
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
