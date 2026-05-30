package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.sql;
import module java.base;
import module ninja.javahacker.annotimpler.sql;

public final class ParameterSetWithValues {

    @Getter
    @NonNull
    private final ParameterSet set;

    @NonNull
    private final List<? extends SqlNamedParameterWithValue<?>> parameters;

    @NonNull
    @PackagePrivate
    ParameterSetWithValues(
            @NonNull ParameterSet set,
            @NonNull List<? extends SqlNamedParameterWithValue<?>> parameters)
            throws SQLException
    {
        checkNotNull(set);
        checkNotNull(parameters);
        this.set = set;
        this.parameters = parameters;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + set.methodName() + " - " + parameters;
    }

    @NonNull
    private List<Object> state() {
        return List.of(set, parameters);
    }

    @Override
    public int hashCode() {
        return state().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ParameterSetWithValues ps && Objects.equals(this.state(), ps.state());
    }

    public void fillIn(@NonNull NamedParameterStatement ps) throws SQLException {
        for (var p : parameters) {
            p.handle(ps);
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
