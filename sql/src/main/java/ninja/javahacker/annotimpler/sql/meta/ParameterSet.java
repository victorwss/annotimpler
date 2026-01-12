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
    private final ParsedQuery query;

    @Getter
    @NonNull
    private final Method m;

    @NonNull
    private final List<? extends SqlNamedParameter<?>> parameters;

    public ParameterSet(@NonNull ParsedQuery query, @NonNull Method m) {
        this.query = query;
        this.m = m;
        this.parameters = SqlNamedParameter.forMethod(m);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + parameters + "]";
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ParameterSet ps && Objects.equals(this.parameters, ps.parameters);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    public static final class ParameterSetWithValues {

        @Getter
        @NonNull
        private final ParsedQuery query;

        @NonNull
        private final List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters;

        @NonNull
        private ParameterSetWithValues(
                @NonNull ParsedQuery query,
                @NonNull List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters)
        {
            if (query == null) throw new AssertionError();
            if (parameters == null) throw new AssertionError();
            this.query = query;
            this.parameters = parameters;
        }

        @NonNull
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[" + parameters + "]";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof ParameterSetWithValues ps
                    && Objects.equals(this.parameters, ps.parameters)
                    && Objects.equals(this.query, ps.query);
        }

        @Override
        public int hashCode() {
            return List.of(parameters, query).hashCode();
        }

        @NonNull
        public void preencher(@NonNull NamedParameterStatement ps) throws SQLException {
            for (var p : parameters) {
                p.handle(ps);
            }
        }
    }

    @NonNull
    public ParameterSetWithValues associar(@NonNull Object... args) {
        var pp = m.getParameters();
        if (args.length != pp.length) throw new IllegalArgumentException();
        @SuppressWarnings("unchecked")
        var wv = parameters.stream().map(p -> ((SqlNamedParameter<Object>) p).withValue(args[p.getIndex()])).toList();
        return new ParameterSetWithValues(query, wv);
    }

    @NonNull
    public static ParameterSet parameters(@NonNull ParsedQuery pq, @NonNull Method m) {
        return new ParameterSet(pq, m);
    }
}
