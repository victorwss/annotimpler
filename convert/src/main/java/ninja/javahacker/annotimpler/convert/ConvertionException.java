package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public class ConvertionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Class<?> in;

    @NonNull
    private final Type out;

    private static String name(@NonNull Type t) {
        if (t instanceof Class<?> k) {
            if (k.isArray()) return name(k.getComponentType()) + "[]";
            return k.getSimpleName();
        }
        if (t instanceof ParameterizedType p) {
            var o = p.getOwnerType();
            var r = p.getRawType();
            var pp = Stream.of(p.getActualTypeArguments()).map(ConvertionException::name).collect(Collectors.joining(", "));
            var oq = o == null ? "" : name(o) + ".";
            var or = name(r);
            return oq + or + "<" + pp + ">";
        }
        return t.getTypeName();
    }

    public ConvertionException(@NonNull Class<?> in, @NonNull Type out) {
        List.of(in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", name(out)), in, out);
    }

    public ConvertionException(@NonNull String message, @NonNull Class<?> in, @NonNull Type out) {
        List.of(message, in, out); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.in = in;
        this.out = out;
    }

    public ConvertionException(@NonNull Throwable cause, @NonNull Class<?> in, @NonNull Type out) {
        List.of(cause, in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", name(out)), cause, in, out);
    }

    public ConvertionException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> in, @NonNull Type out) {
        List.of(message, cause,  in, out); // Force lombok put the null-checks before the constructor call.
        super(message, cause);
        this.in = in;
        this.out = out;
    }

    @NonNull
    public Class<?> getIn() {
        return in;
    }

    @NonNull
    public Type getOut() {
        return out;
    }
}
