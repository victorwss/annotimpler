package ninja.javahacker.annotimpler.sql.methods;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class MethodModel {

    @Getter
    @NonNull
    private final Method method;

    @NonNull
    private final List<? extends ParameterModel> parameters;

    public MethodModel(@NonNull Method method) {
        this.method = method;
        var ps = new ArrayList<ParameterModel>(method.getParameterCount());
        var idx = 0;
        for (var p : method.getParameters()) {
            ps.add(new ParameterModel(this, idx, p));
            idx++;
        }
        this.parameters = List.copyOf(ps);
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

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof MethodModel ps && Objects.equals(this.method, ps.method);
    }

    public static class ParameterModel {

        @Getter
        @NonNull
        private final MethodModel model;

        @Getter
        private final int index;

        @Getter
        @NonNull
        private final Type type;

        @Getter
        @NonNull
        private final String name;

        public ParameterModel(@NonNull MethodModel model, int index, @NonNull Parameter parameter) {
            this.model = model;
            this.index = index;
            this.type = parameter.getParameterizedType();
            this.name = parameter.getName();
        }
    }
}
