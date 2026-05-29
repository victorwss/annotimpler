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
    private final SqlFactory.ParsedSqlSupplier supplier;

    @NonNull
    private final List<? extends SqlNamedParameter<?>> parameters;

    public ParameterSet(boolean preValidate, @NonNull Method method) throws BadImplementationException {
        this.method = method;
        this.parameters = SqlNamedParameter.forMethod(method);
        this.supplier = SqlFactory.find(method);
        if (preValidate) {
            try {
                var query = supplier.get();
                testParameters(query.params().keySet());
            } catch (SQLException e) {
                throw new BadImplementationException("SQL prevalidation failed.", e, ParameterSet.class);
            }
        }
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
    public ParameterSetWithValues withValues(boolean validate, @NonNull Object... args) throws SQLException {
        var pp = method.getParameters();
        if (args.length != pp.length) throw new IllegalArgumentException();
        @SuppressWarnings("unchecked")
        var wv = parameters.stream().map(p -> ((SqlNamedParameter<Object>) p).withValue(args[p.getIndex()])).toList();
        return new ParameterSetWithValues(validate, this, wv);
    }

    private boolean testParameters(@NonNull Set<String> keys) {
        for (var p : parameters) {
            if (!p.testParameter(keys)) return false;
        }
        return true;
    }

    public static final class ParameterSetWithValues {

        @Getter
        @NonNull
        private final ParameterSet set;

        @NonNull
        private final ParsedQuery query;

        @NonNull
        private final List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters;

        @NonNull
        private ParameterSetWithValues(
                boolean validate,
                @NonNull ParameterSet set,
                @NonNull List<? extends SqlNamedParameter.SqlNamedParameterWithValue<?>> parameters)
                throws SQLException
        {
            checkNotNull(set);
            checkNotNull(parameters);
            this.set = set;
            this.parameters = parameters;
            this.query = set.supplier.get();
            if (validate) set.testParameters(query.params().keySet());
        }

        @NonNull
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " - " + name(set.method) + " - " + parameters;
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

        public String parsed() {
            return query.parsed();
        }

        public Map<String, List<Integer>> params() {
            return query.params();
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
