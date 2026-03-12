package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum StringConverter implements Converter<String> {
    INSTANCE;

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
    public Optional<String> from(float in) {
        return Optional.of(String.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<String> from(double in) {
        return Optional.of(String.valueOf(in));
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
        return Optional.of(in.format(LocalTimeConverter.FORMATTER_T));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull LocalDateTime in) {
        return Optional.of(in.format(LocalDateTimeConverter.FORMATTER_DT));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetTime in) {
        return Optional.of(in.format(OffsetTimeConverter.FORMATTER_TZ));
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.format(OffsetDateTimeConverter.FORMATTER_DTZ));
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
            throw new ConvertionException(BAD, x, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(BAD, x, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(BAD, x, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull SQLXML in) throws ConvertionException {
        try {
            return Optional.of(in.getString());
        } catch (SQLException x) {
            throw new ConvertionException(BAD, x, String.class);
        }
    }

    @NonNull
    @Override
    public Optional<String> from(@NonNull RowId in) {
        return Optional.of(new BigInteger(in.getBytes()).toString());
    }
}
