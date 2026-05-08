package ninja.javahacker.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public final class ControlledMock<E> {
    @Getter
    private final E mock;

    @Getter
    private final Class<E> iface;

    @Getter
    @Setter(onParam_ = {@NonNull})
    private InvocationHandler handler;

    @SuppressWarnings("unchecked")
    private ControlledMock(@NonNull Class<E> iface) {
        this.iface = iface;
        this.handler = (i, m, a) -> {
            throw new AssertionError(m);
        };
        var cl = Thread.currentThread().getContextClassLoader();
        this.mock = (E) Proxy.newProxyInstance(cl, new Class<?>[] {iface}, (i, m, a) -> this.handler.invoke(i, m, a));
    }

    public static <E> ControlledMock<E> mock(@NonNull Class<E> iface) {
        return new ControlledMock<>(iface);
    }
}