package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public class ConvertionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Class<?> in;

    @NonNull
    private final Class<?> out;

    public ConvertionException(@NonNull Class<?> in, @NonNull Class<?> out) {
        List.of(in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", out.getSimpleName()), in, out);
    }

    public ConvertionException(@NonNull String message, @NonNull Class<?> in, @NonNull Class<?> out) {
        List.of(message, in, out); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.in = in;
        this.out = out;
    }

    public ConvertionException(@NonNull Throwable cause, @NonNull Class<?> in, @NonNull Class<?> out) {
        List.of(cause, in, out); // Force lombok put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", out.getSimpleName()), cause, in, out);
    }

    public ConvertionException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> in, @NonNull Class<?> out) {
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
    public Class<?> getOut() {
        return out;
    }
}
