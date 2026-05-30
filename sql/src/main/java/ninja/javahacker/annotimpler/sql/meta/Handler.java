package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

@PackagePrivate
@FunctionalInterface
@SuppressWarnings({"checkstyle:ParenPad"})
interface Handler<T> {

    public static final Map<Class<?>, Handler<?>> ENTRIES = Map.ofEntries(
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
            entry(LocalDate     .class, NamedParameterStatement::setLocalDate                 ),
            entry(LocalDateTime .class, NamedParameterStatement::setLocalDateTime             ),
            entry(LocalTime     .class, NamedParameterStatement::setLocalTime                 ),
            entry(OptionalInt   .class, NamedParameterStatement::setInt                       ),
            entry(OptionalLong  .class, NamedParameterStatement::setLong                      ),
            entry(OptionalDouble.class, NamedParameterStatement::setDouble                    )
    );

    public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

    @NonNull
    public default NamedHandler<T> named(@NonNull String name) {
        return (@NonNull NamedParameterStatement ps, @NonNull T value) -> {
            checkNotNull(ps);
            checkNotNull(value);
            handle(ps, name, value);
        };
    }

    @NonNull
    private static <E> Map.Entry<Class<?>, Handler<?>> entry(@NonNull Class<E> k, @NonNull Handler<E> h, int type) {
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
            checkNotNull(ps);
            checkNotNull(name);
            checkNotNull(value);
            if (value == null) {
                ps.setNull(name, type);
            } else {
                h.handle(ps, name, value);
            }
        });
    }

    @NonNull
    private static <E> Map.Entry<Class<?>, Handler<?>> entry(@NonNull Class<E> k, @NonNull Handler<E> h) {
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, h);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}