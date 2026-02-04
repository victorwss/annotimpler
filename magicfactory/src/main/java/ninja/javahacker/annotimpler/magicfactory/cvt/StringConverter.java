package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class StringConverter implements Converter<String> {

    private static final DateTimeFormatter FORMATTER_DT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_D = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_T = DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_DTZ = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss xxxxx")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_TZ = DateTimeFormatter
            .ofPattern("HH:mm:ss xxxxx")
            .withResolverStyle(ResolverStyle.STRICT);

    public StringConverter() {
    }

    @Override
    public String from(boolean in) {
        return String.valueOf(in);
    }

    @Override
    public String from(byte in) {
        return String.valueOf(in);
    }

    @Override
    public String from(short in) {
        return String.valueOf(in);
    }

    @Override
    public String from(int in) {
        return String.valueOf(in);
    }

    @Override
    public String from(long in) {
        return String.valueOf(in);
    }

    @Override
    public String from(float in) {
        return String.valueOf(in);
    }

    @Override
    public String from(double in) {
        return String.valueOf(in);
    }

    @Override
    public String from(@NonNull BigDecimal in) {
        return in.toPlainString();
    }

    @Override
    public String from(@NonNull LocalDate in) {
        return in.format(FORMATTER_D);
    }

    @Override
    public String from(@NonNull LocalTime in) {
        return in.format(FORMATTER_T);
    }

    @Override
    public String from(@NonNull LocalDateTime in) {
        return in.format(FORMATTER_DT);
    }

    @Override
    public String from(@NonNull OffsetTime in) {
        return in.format(FORMATTER_TZ);
    }

    @Override
    public String from(@NonNull OffsetDateTime in) {
        return in.format(FORMATTER_DTZ);
    }

    @Override
    public String from(@NonNull String in) {
        return in;
    }

    @Override
    public String from(@NonNull byte[] in) {
        return new String(in, StandardCharsets.UTF_8);
    }

    @Override
    public String from(@NonNull Blob in) {
        try {
            return new String(in.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String from(@NonNull Clob in) {
        try {
            return in.getCharacterStream().readAllAsString();
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String from(@NonNull NClob in) {
        try {
            return in.getCharacterStream().readAllAsString();
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String from(@NonNull SQLXML in) {
        try {
            return in.getString();
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String from(@NonNull RowId in) {
        return new BigInteger(in.getBytes()).toString();
    }
}
