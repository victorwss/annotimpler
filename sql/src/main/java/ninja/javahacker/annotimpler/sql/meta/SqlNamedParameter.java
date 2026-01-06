package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings({"checkstyle:ParenPad"})
public record SqlNamedParameter(@NonNull Type type, boolean flat) {

    @FunctionalInterface
    public static interface NameHandler<T> {
        public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;
    }

    @FunctionalInterface
    public static interface NameChecker {
        public void check(@NonNull String name);
    }

    private static final Map<@NonNull Class<?>, @NonNull NameHandler<?>> ENTRIES = Map.ofEntries(
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

    @NonNull
    private static <E> Map.Entry<Class<?>, NameHandler<?>> entry(@NonNull Class<E> k, @NonNull NameHandler<E> h, int type) {
        return Map.entry(k, (NamedParameterStatement ps, String name, E value) -> {
            if (value == null) {
                ps.setNull(name, type);
            } else {
                h.handle(ps, name, value);
            }
        });
    }

    @NonNull
    private static <E> Map.Entry<Class<?>, NameHandler<?>> entry(@NonNull Class<E> k, @NonNull NameHandler<E> h) {
        return Map.entry(k, h);
    }

    public record SqlNamedParameterWithValue(@NonNull Type type, @Nullable Object value, boolean flat) {

        @NonNull
        private static <E extends Record> NameHandler<E> forRecordHandler(@NonNull Class<E> k, boolean flat) {
            var rcs = k.getRecordComponents();
            var single = rcs.length == 1;
            return (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
                for (var rc : rcs) {
                    var rct = rc.getType();
                    Object cmp;
                    try {
                        cmp = rc.getAccessor().invoke(value);
                    } catch (IllegalAccessException | InvocationTargetException x) {
                        throw new AssertionError(x);
                    }
                    var paramName = single ? name : flat ? rc.getName() : name + "::" + rc.getName();
                    new SqlNamedParameterWithValue(rct, cmp, flat).handle(ps, paramName);
                }
            };
        }

        @NonNull
        private static <E extends Enum<E>> NameHandler<E> forEnum(@NonNull Class<E> k) {
            return (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
                if (value == null) {
                    ps.setNull(name, Types.INTEGER);
                } else {
                    ps.setInt(name, value.ordinal());
                }
            };
        }

        @SuppressWarnings("unchecked")
        public void handle(@NonNull NamedParameterStatement ps, @NonNull String name) throws SQLException {
            switch (type) {
                case Class<?> c -> {
                    NameHandler<?> h = Record.class.isAssignableFrom(c) ? forRecordHandler(c.asSubclass(Record.class), flat)
                            : Enum.class.isAssignableFrom(c) ? forEnum(c.asSubclass(Enum.class))
                            : ENTRIES.get(c);
                    if (h == null) throw new UnsupportedOperationException("" + c.getName());
                    ((NameHandler<Object>) h).handle(ps, name, value);
                }
                case ParameterizedType p -> {
                    if (p.getRawType() != Optional.class) throw new UnsupportedOperationException();
                    var innerValue = value == null ? null : ((Optional<?>) value).orElse(null);
                    new SqlNamedParameterWithValue(p.getActualTypeArguments()[0], innerValue, flat).handle(ps, name);
                }
                default -> throw new UnsupportedOperationException("" + type);
            }
        }
    }

    @NonNull
    private static NameChecker forRecordChecker(@NonNull Class<?> k, boolean flat) {
        var rcs = k.getRecordComponents();
        var single = rcs.length == 1;
        return (@NonNull String name) -> {
            for (var rc : rcs) {
                var rct = rc.getType();
                var paramName = single ? name : flat ? rc.getName() : name + "::" + rc.getName();
                new SqlNamedParameter(rct, flat).check(paramName);
            }
        };
    }

    @NonNull
    public void check(@NonNull String name) {
        switch (type) {
            case Class<?> c -> {
                NameChecker h = Record.class.isAssignableFrom(c) ? forRecordChecker(c.asSubclass(Record.class), flat)
                        : (Enum.class.isAssignableFrom(c) || ENTRIES.containsKey(c) ? x -> {} : null);
                if (h == null) throw new UnsupportedOperationException("" + c.getName());
                h.check(name);
            }
            case ParameterizedType p -> {
                if (p.getRawType() != Optional.class) throw new UnsupportedOperationException();
                new SqlNamedParameter(p.getActualTypeArguments()[0], flat).check(name);
            }
            default -> throw new UnsupportedOperationException("" + type);
        }
    }

    @NonNull
    public SqlNamedParameterWithValue withValue(@Nullable Object valor) {
        return new SqlNamedParameterWithValue(type, valor, flat);
    }

    @NonNull
    public static SqlNamedParameter forClass(@NonNull Class<?> type, boolean flat) {
        return new SqlNamedParameter(type, flat);
    }
}
