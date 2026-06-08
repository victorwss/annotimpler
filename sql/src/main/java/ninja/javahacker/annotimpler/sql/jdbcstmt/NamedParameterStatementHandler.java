package ninja.javahacker.annotimpler.sql.jdbcstmt;

import ninja.javahacker.annotimpler.sql.meta.ParameterReceiver;
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
            entry(LocalDate     .class, NamedParameterStatement::setLocalDate                 ),
            entry(LocalDateTime .class, NamedParameterStatement::setLocalDateTime             ),
            entry(LocalTime     .class, NamedParameterStatement::setLocalTime                 ),
            entry(OptionalInt   .class, NamedParameterStatement::setInt                       ),
            entry(OptionalLong  .class, NamedParameterStatement::setLong                      ),
            entry(OptionalDouble.class, NamedParameterStatement::setDouble                    ),
            nully()
    );

    public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

    @SuppressWarnings("unchecked")
    public static <K> NamedParameterStatementHandler<K> forClass(@NonNull Class<K> k) {
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
            checkNotNull(value);
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
    private static Map.Entry<Class<Void>, NamedParameterStatementHandler<Void>> nully() {
        return Map.entry(Void.class, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable Void value) -> {
            checkNotNull(ps);
            checkNotNull(name);
            ps.setNull(name, Types.NULL);
        });
    }

    public static ParameterReceiver forJdbc(@NonNull NamedParameterStatement ps) {
        return new ParameterReceiver() {
            @Override
            public void receiveNull(String name, Class<?> type) throws SQLException {
                forClass(type).handle(ps, name, null);
            }

            @Override
            public void receive(String name, Object value) throws SQLException {
                receiveIn(name, value.getClass(), value);
            }

            private <K> void receiveIn(String name, Class<K> k, Object value) throws SQLException {
                forClass(k).handle(ps, name, k.cast(value));
            }
        };
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}