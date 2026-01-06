package ninja.javahacker.annotimpler.sql.stmt;

import lombok.NonNull;
import lombok.experimental.Delegate;

import module java.base;
import module java.sql;

final class InternalNamedParameterStatement implements NamedParameterStatement {

    @Delegate(types = PreparedStatement.class)
    private final PreparedStatement statement;

    private final Map<String, List<Integer>> paramMap;

    public InternalNamedParameterStatement(@NonNull PreparedStatement statement, @NonNull Map<String, List<Integer>> params) {
        var copy = params.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> List.copyOf(e.getValue())));
        this.statement = statement;
        this.paramMap = copy;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // É garantido imutável.
    public Map<String, List<Integer>> getIndexes() {
        return paramMap;
    }
}
