package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

public final class ParameterSet {

    @Getter
    @NonNull
    private final Method method;

    @NonNull
    private final ParameterReceiver.NamedAcceptor1 strategy;

    public ParameterSet(@NonNull Method method) throws BadImplementationException {
        this.method = method;
        this.strategy = ParameterSetStrategy.makeStrategy(method);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + methodName();
    }

    @NonNull
    private List<Object> state() {
        return List.of(method);
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
    public ParameterReceiver.Acceptor2 withValues(@NonNull Object... args) throws ParameterReceiver.IllegalValueException {
        return strategy.handle(args);
    }

    @NonNull
    public List<String> paramNames() {
        return strategy.paramNames();
    }
}
