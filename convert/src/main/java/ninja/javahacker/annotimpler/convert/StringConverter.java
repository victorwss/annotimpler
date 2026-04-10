package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum StringConverter implements Converter<String> {
    INSTANCE;

    @NonNull
    @Override
    public Class<String> getType() {
        return String.class;
    }

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
        if (Float.floatToRawIntBits(in) == Integer.MIN_VALUE) return Optional.of("-0");
        return BigDecimalConverter.INSTANCE.from(in).map(bd -> bd.toPlainString());
    }

    @NonNull
    @Override
    public Optional<String> from(double in) throws ConvertionException {
        if (in == Double.POSITIVE_INFINITY) return Optional.of("Infinity");
        if (in == Double.NEGATIVE_INFINITY) return Optional.of("-Infinity");
        if (Double.isNaN(in)) return Optional.of("NaN");
        if (Double.doubleToRawLongBits(in) == Long.MIN_VALUE) return Optional.of("-0");
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
        return Optional.of(MultiFormatters.format(in));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalTime in) {
        return Optional.of(MultiFormatters.format(in));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalDateTime in) {
        return Optional.of(MultiFormatters.format(in));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetTime in) {
        return Optional.of(MultiFormatters.format(in));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetDateTime in) {
        return Optional.of(MultiFormatters.format(in));
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
            throw new ConvertionException(x, Blob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, Clob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, NClob.class, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull SQLXML in) throws ConvertionException {
        try {
            return Optional.of(in.getString());
        } catch (SQLException x) {
            throw new ConvertionException(x, SQLXML.class, String.class);
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
