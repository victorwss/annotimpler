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

    @Getter
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
        return this.getClass().getSimpleName() + " - " + methodName();
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
    public String methodName() {
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

    public boolean testParameters(@NonNull Set<String> keys) {
        for (var p : parameters) {
            if (!p.testParameter(keys)) return false;
        }
        return true;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
