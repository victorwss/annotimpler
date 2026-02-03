package ninja.javahacker.annotimpler.magicfactory;

import lombok.NonNull;

import module java.base;
import module java.sql;

public interface Converter<E> {

    public default E fromNull() {
        return null;
    }

    public default E from(boolean in) {
        throw new UnsupportedOperationException();
    }

    public default E from(byte in) {
        throw new UnsupportedOperationException();
    }

    public default E from(short in) {
        throw new UnsupportedOperationException();
    }

    public default E from(int in) {
        throw new UnsupportedOperationException();
    }

    public default E from(long in) {
        throw new UnsupportedOperationException();
    }

    public default E from(float in) {
        throw new UnsupportedOperationException();
    }

    public default E from(double in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull BigDecimal in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull LocalDate in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull LocalTime in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull LocalDateTime in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull OffsetTime in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull OffsetDateTime in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull String in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull byte[] in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull Blob in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull Clob in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull NClob in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull java.sql.Array in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull Ref in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull SQLXML in) {
        throw new UnsupportedOperationException();
    }

    public default E from(@NonNull RowId in) {
        throw new UnsupportedOperationException();
    }
}
