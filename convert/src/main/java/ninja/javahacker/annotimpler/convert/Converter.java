package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module java.sql;

/// A type converter that transforms an input value of any supported type into an optional value of type `E`.
///
/// Implementations override the specific `from(X)` overloads they support. The default implementation
/// of each `from(X)` method throws [ConvertionException]. The main entry point is [#fromObj(Object)],
/// which dispatches to the appropriate overload using a pattern-matching switch.
///
/// @param <E> The target type this converter produces.
public interface Converter<E> {

    /// Dispatches `in` to the appropriate `from(X)` overload using a pattern-matching switch.
    ///
    /// If `in` is `null`, delegates to [#fromNull()]. Otherwise dispatches to the matching typed
    /// overload. If the runtime type of `in` is not one of the supported types, throws [ConvertionException].
    ///
    /// @param in The input value to convert, or `null`.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If the runtime type of `in` is not supported by this converter.
    @NonNull
    @SuppressFBWarnings("CC_CYCLOMATIC_COMPLEXITY")
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
            default -> throw new ConvertionException("Unsupported Type: " + in.getClass().getName() + ".", in.getClass(), getType());
        };
    }

    private static boolean typeFilter(@NonNull Type t) {
        checkNotNull(t); // Check recognized by lombok.
        return t instanceof ParameterizedType p && Converter.class.isAssignableFrom((Class<?>) p.getRawType());
    }

    @SuppressWarnings("InfiniteRecursion")
    private static boolean isInvariant(@NonNull Type t) {
        checkNotNull(t); // Check recognized by lombok.
        return t instanceof Class<?>
                || t instanceof ParameterizedType
                || (t instanceof GenericArrayType ga && isInvariant(ga.getGenericComponentType()));
    }

    private static Stream<Type> getAllInterfaces(@NonNull Type t) {
        checkNotNull(t); // Check recognized by lombok.
        assertExtendable(t);

        var e = (Class<?>) (t instanceof ParameterizedType p ? p.getRawType() : t);

        var a = List.of(e.getGenericInterfaces());
        var sc = e.getGenericSuperclass();
        var b = sc == null ? Stream.<Type>of() : getAllInterfaces(sc);
        var c = a.stream().flatMap(Converter::getAllInterfaces);

        return Stream.of(a.stream(), b, c, Stream.of(e)).flatMap(Function.identity());
    }

    /// Returns the target type `E` that this converter produces.
    ///
    /// The default implementation discovers the type by inspecting the generic superinterface
    /// declarations using reflection. Throws [IllegalStateException] if the type cannot be determined;
    /// in that case, the method should be overridden.
    ///
    /// @return The [Type] representing `E`.
    /// @throws IllegalStateException If the type argument cannot be determined via reflection.
    @NonNull
    public default Type getType() {
        return getAllInterfaces(getClass())
                .filter(Converter::typeFilter)
                .map(iface -> ((ParameterizedType) iface).getActualTypeArguments()[0])
                .filter(Converter::isInvariant)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Couldn't determine the type. Please, override this method."));
    }

    /// Called when the input value is `null`. The default implementation returns [Optional#empty()].
    ///
    /// @return An optional representing the converted `null` value.
    /// @throws ConvertionException If this converter does not support `null` input.
    @NonNull
    public default Optional<E> fromNull() throws ConvertionException {
        return Optional.empty();
    }

    /// Converts a `boolean` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `boolean`.
    @NonNull
    public default Optional<E> from(boolean in) throws ConvertionException {
        throw new ConvertionException("Unsupported boolean.", boolean.class, getType());
    }

    /// Converts a `byte` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `byte`.
    @NonNull
    public default Optional<E> from(byte in) throws ConvertionException {
        throw new ConvertionException("Unsupported byte.", byte.class, getType());
    }

    /// Converts a `short` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `short`.
    @NonNull
    public default Optional<E> from(short in) throws ConvertionException {
        throw new ConvertionException("Unsupported short.", short.class, getType());
    }

    /// Converts an `int` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `int`.
    @NonNull
    public default Optional<E> from(int in) throws ConvertionException {
        throw new ConvertionException("Unsupported int.", int.class, getType());
    }

    /// Converts a `long` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `long`.
    @NonNull
    public default Optional<E> from(long in) throws ConvertionException {
        throw new ConvertionException("Unsupported long.", long.class, getType());
    }

    /// Converts a `float` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `float`.
    @NonNull
    public default Optional<E> from(float in) throws ConvertionException {
        throw new ConvertionException("Unsupported float.", float.class, getType());
    }

    /// Converts a `double` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `double`.
    @NonNull
    public default Optional<E> from(double in) throws ConvertionException {
        throw new ConvertionException("Unsupported double.", double.class, getType());
    }

    /// Converts a [BigDecimal] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `BigDecimal`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull BigDecimal in) throws ConvertionException {
        throw new ConvertionException("Unsupported BigDecimal.", BigDecimal.class, getType());
    }

    /// Converts a [LocalDate] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `LocalDate`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull LocalDate in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalDate.", LocalDate.class, getType());
    }

    /// Converts a [LocalTime] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `LocalTime`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull LocalTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalTime.", LocalTime.class, getType());
    }

    /// Converts a [LocalDateTime] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `LocalDateTime`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull LocalDateTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported LocalDateTime.", LocalDateTime.class, getType());
    }

    /// Converts an [OffsetTime] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `OffsetTime`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull OffsetTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported OffsetTime.", OffsetTime.class, getType());
    }

    /// Converts an [OffsetDateTime] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `OffsetDateTime`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull OffsetDateTime in) throws ConvertionException {
        throw new ConvertionException("Unsupported OffsetDateTime.", OffsetDateTime.class, getType());
    }

    /// Converts a [String] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `String`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull String in) throws ConvertionException {
        throw new ConvertionException("Unsupported String.", String.class, getType());
    }

    /// Converts a `byte[]` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `byte[]`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull byte[] in) throws ConvertionException {
        throw new ConvertionException("Unsupported byte[].", byte[].class, getType());
    }

    /// Converts a [Blob] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `Blob`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull Blob in) throws ConvertionException {
        throw new ConvertionException("Unsupported Blob.", Blob.class, getType());
    }

    /// Converts a [Clob] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `Clob`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull Clob in) throws ConvertionException {
        throw new ConvertionException("Unsupported Clob.", Clob.class, getType());
    }

    /// Converts an [NClob] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `NClob`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull NClob in) throws ConvertionException {
        throw new ConvertionException("Unsupported NClob.", NClob.class, getType());
    }

    /// Converts an [SQLXML] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `SQLXML`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull SQLXML in) throws ConvertionException {
        throw new ConvertionException("Unsupported SQLXML.", SQLXML.class, getType());
    }

    /// Converts a [RowId] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `RowId`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull RowId in) throws ConvertionException {
        throw new ConvertionException("Unsupported RowId.", RowId.class, getType());
    }

    /// Converts a `java.sql.Array` value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `java.sql.Array`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull java.sql.Array in) throws ConvertionException {
        throw new ConvertionException("Unsupported Array.", java.sql.Array.class, getType());
    }

    /// Converts a [Struct] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `Struct`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull Struct in) throws ConvertionException {
        throw new ConvertionException("Unsupported Struct.", Struct.class, getType());
    }

    /// Converts a [Ref] value to `E`. The default implementation always throws [ConvertionException].
    ///
    /// @param in The value to convert.
    /// @return An optional containing the converted value.
    /// @throws ConvertionException If this converter does not support conversion from `Ref`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public default Optional<E> from(@NonNull Ref in) throws ConvertionException {
        throw new ConvertionException("Unsupported Ref.", Ref.class, getType());
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }

    @Generated
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    private static void assertExtendable(Type t) {
        if (t instanceof Class<?>) return;
        if (t instanceof ParameterizedType) return;
        throw new AssertionError(t);
    }
}
