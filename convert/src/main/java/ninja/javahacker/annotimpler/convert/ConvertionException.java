package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public class ConvertionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Class<?> in;

    @NonNull
    private final Type out;

    public ConvertionException(@NonNull Class<?> in, @NonNull Type out) {
        List.of(in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", TypeName.of(out)), in, out);
    }

    public ConvertionException(@NonNull String message, @NonNull Class<?> in, @NonNull Type out) {
        List.of(message, in, out); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.in = in;
        this.out = out;
    }

    public ConvertionException(@NonNull Throwable cause, @NonNull Class<?> in, @NonNull Type out) {
        List.of(cause, in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", TypeName.of(out)), cause, in, out);
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
