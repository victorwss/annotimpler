package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

@Generated
public final class Asserts {
    private Asserts() {
        throw new UnsupportedOperationException();
    }

    public static void asserts(boolean b) {
        if (!b) throw new AssertionError();
    }

    @NonNull
    public static <E> E nonNull(@Nullable E x) {
        if (x == null) throw new AssertionError();
        return x;
    }
}
