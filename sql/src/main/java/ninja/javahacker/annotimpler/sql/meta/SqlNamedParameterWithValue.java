package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.sql;
import module java.base;
import module ninja.javahacker.annotimpler.sql;

public class SqlNamedParameterWithValue<T> {
    @NonNull
    private final SqlNamedParameter<T> inner;

    @Getter
    @Nullable
    private final T value;

    @PackagePrivate
    SqlNamedParameterWithValue(@NonNull SqlNamedParameter<T> inner, @Nullable T value) {
        checkNotNull(inner);
        if (!inner.accept(value)) throw new IllegalArgumentException();
        this.inner = inner;
        this.value = value;
    }

    public void handle(@NonNull NamedParameterStatement ps) throws SQLException {
        inner.handle(ps, value);
    }

    public int getIndex() {
        return inner.getIndex();
    }

    @NonNull
    public Type getType() {
        return inner.getType();
    }

    @NonNull
    public String getName() {
        return inner.getName();
    }

    public boolean isFlat() {
        return inner.isFlat();
    }

    @NonNull
    private List<Object> state() {
        return Stream.concat(inner.state().stream(), Stream.of(value)).toList();
    }

    @Override
    public int hashCode() {
        return state().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof SqlNamedParameterWithValue<?> ps && Objects.equals(this.state(), ps.state());
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + state();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
