package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class ParameterSet {

    @Getter
    @NonNull
    private final Method method;

    @NonNull
    private final List<? extends SqlNamedParameter<?>> parameters;

    public ParameterSet(@NonNull Method method) {
        this.method = method;
        this.parameters = SqlNamedParameter.forMethod(method);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + name(method);
    }

    @NonNull
    private List<Object> state() {
        return List.of(method, parameters);
    }

    @Override
    public int hashCode() {
        return state().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ParameterSet ps && Objects.equals(this.state(), ps.state());
    }

    @NonNull
    private static String name(@NonNull Method method) {
        checkNotNull(method);
        return NameDictionary.global().getSimplifiedGenericString(method, false);
    }

    @NonNull
    public ParameterSetWithValues withValues(@NonNull Object... args) {
        var pp = method.getParameters();
        if (args.length != pp.length) throw new IllegalArgumentException();
        @SuppressWarnings("unchecked")
        var wv = parameters.stream().map(p -> ((SqlNamedParameter<Object>) p).withValue(args[p.getIndex()])).toList();
        return new ParameterSetWithValues(method, wv);
    }

    public static final class ParameterSetWithValues {

        @Getter
        @NonNull
        private final Method method;

        @NonNull
        private final List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters;

        @NonNull
        private ParameterSetWithValues(
                @NonNull Method method,
                @NonNull List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters)
        {
            checkNotNull(method);
            checkNotNull(parameters);
            this.method = method;
            this.parameters = parameters;
        }

        @NonNull
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " - " + name(method) + " - " + parameters;
        }

        @NonNull
        private List<Object> state() {
            return List.of(method, parameters);
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
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
