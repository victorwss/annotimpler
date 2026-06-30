package ninja.javahacker.test;

@FunctionalInterface
public interface Sneaky<E> {
    public E get() throws Throwable;

    public default E sneakyGet() {
        try {
            return get();
        } catch (Throwable t) {
            sneakyThrow(t);
            throw new AssertionError();
        }
    }

    public static void sneakyThrow(Throwable t) {
        Sneaky.<RuntimeException>sneakyThrowInner(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrowInner(Throwable t) throws T {
        throw (T) t; 
    }
}
