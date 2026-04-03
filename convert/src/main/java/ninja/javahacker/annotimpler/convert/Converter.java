package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module java.sql;

public interface Converter<E> {

    @NonNull
    public default Optional<E> fromObj(@Nullable Object in) throws ConvertionException {
        return switch (in) {
            case null -> fromNull();
            case Boolean b -> from(b);
            case Byte b -> from(b);
            case Short b -> from(b);
            case Integer b -> from(b);
            case Long b -> from(b);
            case Float b -> from(b);
            case Double b -> from(b);
            case BigDecimal b -> from(b);
            case LocalDate b -> from(b);
            case LocalTime b -> from(b);
            case LocalDateTime b -> from(b);
            case OffsetTime b -> from(b);
            case OffsetDateTime b -> from(b);
            case String b -> from(b);
            case byte[] b -> from(b);
            case Blob b -> from(b);
            case NClob b -> from(b);
            case Clob b -> from(b);
            case SQLXML b -> from(b);
            case RowId b -> from(b);
            case Ref b -> from(b);
            case Struct b -> from(b);
            case java.sql.Array b -> from(b);
            default -> throw new ConvertionException("Unsupported Type: " + in.getClass().getName(), in.getClass(), getType());
        };
    }

    @NonNull
    public Class<E> getType();

    @NonNull
    public default Optional<E> fromNull() throws ConvertionException {
        return Optional.empty();
    }

    @NonNull
    public default Optional<E> from(boolean in) throws ConvertionException {
        throw new ConvertionException("Unsupported boolean", boolean.class, getType());
    }

    @NonNull
    public default Optional<E> from(byte in) throws ConvertionException {
        throw new ConvertionException("Unsupported byte", byte.class, getType());
    }

    @NonNull
    public default Optional<E> from(short in) throws ConvertionException {
        throw new ConvertionException("Unsupported short", short.class, getType());
    }

    @NonNull
    public default Optional<E> from(int in) throws ConvertionException {
        throw new ConvertionException("Unsupported int", int.class, getType());
    }

    @NonNull
    public default Optional<E> from(long in) throws ConvertionException {
        throw new ConvertionException("Unsupported long", long.class, getType());
    }

    @NonNull
    public default Optional<E> from(float in) throws ConvertionException {
        throw new ConvertionException("Unsupported float", float.class, getType());
    }

    @NonNull
    public default Optional<E> from(double in) throws ConvertionException {
        throw new ConvertionException("Unsupported double", double.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull BigDecimal in) throws ConvertionException {
        throw new ConvertionException("Unsupported BigDecimal", BigDecimal.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull LocalDate in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalDate", LocalDate.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull LocalTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalTime", LocalTime.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull LocalDateTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalDateTime", LocalDateTime.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull OffsetTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported OffsetTime", OffsetTime.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull OffsetDateTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported OffsetDateTime", OffsetDateTime.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull String in) throws ConvertionException {
        throw new ConvertionException("Unsupported String", String.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull byte[] in) throws ConvertionException {
        throw new ConvertionException("Unsupported byte[]", byte[].class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull Blob in) throws ConvertionException {
        throw new ConvertionException("Unsupported Blob", Blob.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull Clob in) throws ConvertionException {
        throw new ConvertionException("Unsupported Clob", Clob.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull NClob in) throws ConvertionException {
        throw new ConvertionException("Unsupported NClob", NClob.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull SQLXML in) throws ConvertionException {
        throw new ConvertionException("Unsupported SQLXML", SQLXML.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull RowId in) throws ConvertionException {
        throw new ConvertionException("Unsupported RowId", RowId.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull java.sql.Array in) throws ConvertionException {
        throw new ConvertionException("Unsupported Array", java.sql.Array.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull Struct in) throws ConvertionException {
        throw new ConvertionException("Unsupported Struct", Struct.class, getType());
    }

    @NonNull
    public default Optional<E> from(@NonNull Ref in) throws ConvertionException {
        throw new ConvertionException("Unsupported Ref", Ref.class, getType());
    }
}
