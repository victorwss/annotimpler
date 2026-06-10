package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

@PackagePrivate
@FunctionalInterface
@SuppressWarnings({"checkstyle:ParenPad"})
interface NamedParameterStatementHandler<T> {

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

    public static final Map<Class<?>, Object> EMPTY = Map.ofEntries(
            Map.entry(OptionalInt   .class, OptionalInt   .empty()),
            Map.entry(OptionalLong  .class, OptionalLong  .empty()),
            Map.entry(OptionalDouble.class, OptionalDouble.empty())
    );

    public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

    @SuppressWarnings("unchecked")
    public static <K> NamedParameterStatementHandler<K> forClass(@NonNull Class<K> k) {
        checkNotNull(k);
        var h = (NamedParameterStatementHandler<K>) NamedParameterStatementHandler.ENTRIES.get(k);
        if (h == null) throw new UnsupportedOperationException();
        return h;
    }

    @NonNull
    private static <E> Map.Entry<Class<E>, NamedParameterStatementHandler<E>> entry(
            @NonNull Class<E> k,
            @NonNull NamedParameterStatementHandler<E> h,
            int type)
    {
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
            checkNotNull(ps);
            checkNotNull(name);
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
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, h);
    }

    @NonNull
    private static Map.Entry<Class<Void>, NamedParameterStatementHandler<Void>> nully(boolean k) {
        return Map.entry(k ? void.class : Void.class, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable Void value) -> {
            checkNotNull(ps);
            checkNotNull(name);
            ps.setNull(name, Types.NULL);
        });
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}