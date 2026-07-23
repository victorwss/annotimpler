package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;

/// Binds a single Java value of type `T` onto a named parameter of a [NamedParameterStatement].
///
/// Instances are looked up by target Java type via [#forClass(Class)], which resolves the
/// concrete handler from the [#ENTRIES] table built for all supported wrapper/primitive types.
///
/// @param <T> The Java type of the value this handler knows how to bind.
@PackagePrivate
@FunctionalInterface
@SuppressWarnings({"checkstyle:ParenPad"})
interface NamedParameterStatementHandler<T> {

    /// Maps each supported Java type to the [NamedParameterStatementHandler] able to bind values of that type.
    public static final Map<Class<?>, NamedParameterStatementHandler<?>> ENTRIES = Map.ofEntries(
            entry(boolean       .class, NamedParameterStatement::setBoolean   , Types.BOOLEAN ),
            entry(Boolean       .class, NamedParameterStatement::setBoolean   , Types.BOOLEAN ),
            entry(byte          .class, NamedParameterStatement::setByte      , Types.TINYINT ),
            entry(Byte          .class, NamedParameterStatement::setByte      , Types.TINYINT ),
            entry(short         .class, NamedParameterStatement::setShort     , Types.SMALLINT),
            entry(Short         .class, NamedParameterStatement::setShort     , Types.SMALLINT),
            entry(int           .class, NamedParameterStatement::setInt       , Types.INTEGER ),
            entry(Integer       .class, NamedParameterStatement::setInt       , Types.INTEGER ),
            entry(long          .class, NamedParameterStatement::setLong      , Types.BIGINT  ),
            entry(Long          .class, NamedParameterStatement::setLong      , Types.BIGINT  ),
            entry(float         .class, NamedParameterStatement::setFloat     , Types.FLOAT   ),
            entry(Float         .class, NamedParameterStatement::setFloat     , Types.FLOAT   ),
            entry(double        .class, NamedParameterStatement::setDouble    , Types.DOUBLE  ),
            entry(Double        .class, NamedParameterStatement::setDouble    , Types.DOUBLE  ),
            entry(BigDecimal    .class, NamedParameterStatement::setBigDecimal                ),
            entry(String        .class, NamedParameterStatement::setString                    ),
            entry(byte[]        .class, NamedParameterStatement::setBytes                     ),
            entry(LocalDate     .class, NamedParameterStatement::setLocalDate                 ),
            entry(LocalDateTime .class, NamedParameterStatement::setLocalDateTime             ),
            entry(LocalTime     .class, NamedParameterStatement::setLocalTime                 ),
            entry(OffsetDateTime.class, NamedParameterStatement::setOffsetDateTime            ),
            entry(ZonedDateTime .class, NamedParameterStatement::setZonedDateTime             ),
            entry(OffsetTime    .class, NamedParameterStatement::setOffsetTime                ),
            entry(Instant       .class, NamedParameterStatement::setInstant                   ),
            entry(OptionalInt   .class, NamedParameterStatement::setInt                       ),
            entry(OptionalLong  .class, NamedParameterStatement::setLong                      ),
            entry(OptionalDouble.class, NamedParameterStatement::setDouble                    ),
            nully(false),
            nully(true)
    );

    /// Maps each `Optional`-like type to its empty instance, used as the value substituted when binding an empty optional.
    public static final Map<Class<?>, Object> EMPTY = Map.ofEntries(
            Map.entry(OptionalInt   .class, OptionalInt   .empty()),
            Map.entry(OptionalLong  .class, OptionalLong  .empty()),
            Map.entry(OptionalDouble.class, OptionalDouble.empty())
    );

    /// Binds `value` onto the named parameter `name` of `ps`.
    ///
    /// @param ps The statement to bind the parameter on; must not be `null`.
    /// @param name The parameter name; must not be `null`.
    /// @param value The value to bind; may be `null` to bind SQL `NULL`.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `ps` or `name` is `null`.
    public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

    /// Returns the [NamedParameterStatementHandler] registered for the Java type `k`.
    ///
    /// @param <K> The Java type to look up a handler for.
    /// @param k The class of the target type; must not be `null`.
    /// @return The handler able to bind values of type `k`; never `null`.
    /// @throws UnsupportedOperationException If no handler is registered for `k`.
    /// @throws IllegalArgumentException If `k` is `null`.
    @SuppressWarnings("unchecked")
    public static <K> NamedParameterStatementHandler<K> forClass(@NonNull Class<K> k) {
        checkNotNull(k); // Check recognized by lombok.
        var h = (NamedParameterStatementHandler<K>) ENTRIES.get(k);
        if (h == null) throw new UnsupportedOperationException("Unable to handle " + k.getSimpleName() + ".");
        return h;
    }

    @NonNull
    private static <E> Map.Entry<Class<E>, NamedParameterStatementHandler<E>> entry(
            @NonNull Class<E> k,
            @NonNull NamedParameterStatementHandler<E> h,
            int type)
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(h); // Check recognized by lombok.
        return Map.entry(k, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
            checkNotNull(ps); // Check recognized by lombok.
            checkNotNull(name); // Check recognized by lombok.
            if (value == null) {
                ps.setNull(name, type);
            } else {
                h.handle(ps, name, value);
            }
        });
    }

    @NonNull
    private static <E> Map.Entry<Class<E>, NamedParameterStatementHandler<E>> entry(
            @NonNull Class<E> k,
            @NonNull NamedParameterStatementHandler<E> h)
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(h); // Check recognized by lombok.
        return Map.entry(k, h);
    }

    @NonNull
    private static Map.Entry<Class<Void>, NamedParameterStatementHandler<Void>> nully(boolean k) {
        return Map.entry(k ? void.class : Void.class, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable Void value) -> {
            checkNotNull(ps); // Check recognized by lombok.
            checkNotNull(name); // Check recognized by lombok.
            ps.setNull(name, Types.NULL);
        });
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}