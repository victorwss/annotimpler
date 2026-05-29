package ninja.javahacker.annotimpler.sql.methods;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class ValuedMethodModel {

    @Getter
    @NonNull
    private final Method method;

    @NonNull
    private final List<? extends ParameterModel> parameters;

    public ValuedMethodModel(@NonNull Method method) {
        this.method = method;
        var pp = method.getParameters();
        this.parameters = IntStream.range(0, pp.length).mapToObj(i -> new ParameterModel(i, pp[i])).toList();
    }

    @NonNull
    public String methodName() {
        return NameDictionary.global().getSimplifiedGenericString(method, false);
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
        return other instanceof ValuedMethodModel ps && Objects.equals(this.state(), ps.state());
    }

    public static class ParameterModel {

        @Getter
        private final int index;

        @Getter
        @NonNull
        private final Type type;

        @Getter
        @NonNull
        private final String name;

        public ParameterModel(int index, @NonNull Parameter parameter) {
            this.index = index;
            this.type = parameter.getParameterizedType();
            this.name = parameter.getName();
        }
    }
}
