package ninja.javahacker.annotimpler.sql.stmt;

import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;

@PackagePrivate
final class InternalNamedParameterStatement implements NamedParameterStatement {

    @Delegate(types = PreparedStatement.class)
    private final PreparedStatement statement;

    @NonNull
    private final Map<String, List<Integer>> paramMap;

    public InternalNamedParameterStatement(@NonNull PreparedStatement statement, @NonNull Map<String, List<Integer>> params) {
        if (statement == null) throw new AssertionError();
        if (params == null) throw new AssertionError();
        var copy = params.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> List.copyOf(e.getValue())));
        this.statement = statement;
        this.paramMap = copy;
    }

    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // É garantido imutável.
    public Map<String, List<Integer>> getIndexes() {
        return paramMap;
    }
}
