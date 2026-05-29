package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.Getter;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class SqlNamedParameter<T> {

    @Getter
    private final int index;

    @Getter
    @NonNull
    private final Type type;

    @Getter
    @NonNull
    private final String name;

    @Getter
    private final boolean flat;

    @NonNull
    private final NameHandlerStrategy<T> strategy;

    @SuppressWarnings("unchecked")
    private SqlNamedParameter(int index, @NonNull Type type, @NonNull String name, boolean flat) {
        checkNotNull(type);
        checkNotNull(name);
        this.index = index;
        this.type = type;
        this.name = name;
        this.flat = flat;
        this.strategy = (NameHandlerStrategy<T>) NameHandlerStrategy.makeStrategy(type, name, flat);
    }

    @NonNull
    private static SqlNamedParameter<?> forParam(int index, @NonNull Parameter p) {
        checkNotNull(p);
        return new SqlNamedParameter<>(index, p.getParameterizedType(), p.getName(), p.isAnnotationPresent(Flat.class));
    }

    @NonNull
    public static List<? extends SqlNamedParameter<?>> forMethod(@NonNull Method m) {
        var pp = m.getParameters();
        return IntStream.range(0, pp.length).mapToObj(i -> SqlNamedParameter.forParam(i, pp[i])).toList();
    }

    @NonNull
    @PackagePrivate
    List<Object> state() {
        return List.of(index, type, name, flat);
    }

    @Override
    public int hashCode() {
        return state().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) return true;
        if (!(other instanceof SqlNamedParameter<?> p)) return false;
        return Objects.equals(this.state(), p.state());
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + state();
    }

    @NonNull
    public SqlNamedParameterWithValue<T> withValue(@Nullable T value) {
        return new SqlNamedParameterWithValue<>(this, value);
    }

    public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
        strategy.handle(ps, value);
    }

    public boolean testParameter(@NonNull Set<String> keys) {
        return strategy.testName(keys);
    }

    public boolean accept(@Nullable Object value) {
        return strategy.test(value);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
