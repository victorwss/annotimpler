package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// A [ParameterReceiver.NamedAcceptor1] that delegates binding to a wrapped [ParameterReceiver.Acceptor1]
/// while also exposing the ordered list of SQL parameter names that it manages.
///
/// Instances are not constructed directly. Instead, the various private static `for*` and `make*`
/// factory methods introspect a method parameter, record component or supported type and build up
/// a [ParameterSetStrategy] (possibly composed from other, nested strategies), culminating in the
/// public entry point [#makeStrategy(Method)], which builds the complete parameter-binding strategy
/// for an annotated interface method.
///
/// @see ParameterReceiver#forMethod
@PackagePrivate
final class ParameterSetStrategy implements ParameterReceiver.NamedAcceptor1 {

    /// The object that handles the actual implementation.
    @NonNull
    private final ParameterReceiver.Acceptor1 h;

    /// The ordered list of SQL parameter names managed by this acceptor.
    @NonNull
    private final List<String> paramNames;

    /// Creates a new instance wrapping the given delegate and parameter names.
    ///
    /// @param h The object that handles the actual implementation; must not be `null`.
    /// @param paramNames The ordered list of SQL parameter names managed by this acceptor; must not be `null`.
    /// @throws IllegalArgumentException If `h` or `paramNames` is `null`.
    private ParameterSetStrategy(@NonNull ParameterReceiver.Acceptor1 h, @NonNull List<String> paramNames) {
        checkNotNull(h); // Check recognized by lombok.
        checkNotNull(paramNames); // Check recognized by lombok.
        this.h = h;
        this.paramNames = paramNames;
    }

    /// {@inheritDoc}
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // Known to always be immutable.
    public List<String> paramNames() {
        return paramNames;
    }

    /// {@inheritDoc}
    @Override
    public ParameterReceiver.Acceptor2 handle(@Nullable Object value) throws ParameterReceiver.IllegalValueException {
        return h.handle(value);
    }

    @NonNull
    private static ParameterReceiver.NamedAcceptor1 forRecord(@NonNull Class<? extends Record> k, @NonNull String name, boolean flat)
            throws BadImplementationException
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.
        checkAccessible(k);
        // assert(k.isRecord());

        var rcs = k.getRecordComponents();
        var single = rcs.length == 1;
        var all = new ArrayList<ParameterReceiver.NamedAcceptor1>(rcs.length);
        for (var rc : rcs) {
            all.add(forComponent(rc, k, name, single, flat));
        }

        ParameterReceiver.Acceptor1 h = (@Nullable Object value) -> {
            if (value != null && !k.isInstance(value)) throw new ParameterReceiver.IllegalValueException();
            var acceptors = new ArrayList<ParameterReceiver.Acceptor2>(all.size());
            for (var each : all) {
                acceptors.add(each.handle(value));
            }
            return (@NonNull ParameterReceiver ps) -> {
                checkNotNull(ps); // Would be for Lombok. But Lombok don't look into lambdas.
                for (var each : acceptors) {
                    each.accept(ps);
                }
            };
        };
        var names = all.stream().map(ParameterReceiver.NamedAcceptor1::paramNames).flatMap(List::stream).toList();
        return new ParameterSetStrategy(h, names);
    }

    @NonNull
    private static ParameterReceiver.NamedAcceptor1 forComponent(
            @NonNull RecordComponent rc,
            @NonNull Class<? extends Record> k,
            @NonNull String name,
            boolean single,
            boolean flat)
            throws BadImplementationException
    {
        checkNotNull(rc); // Check recognized by lombok.
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.

        var rct = rc.getGenericType();

        var paramName = single ? name
                : flat ? rc.getName()
                : name + "::" + rc.getName();

        var inner = makeStrategy(rct, paramName, flat);

        ParameterReceiver.Acceptor1 h = (@Nullable Object value) -> {
            if (value == null) return inner.handle(null);
            if (!k.isInstance(value)) throw new ParameterReceiver.IllegalValueException();
            Object innerValue;
            try {
                innerValue = rc.getAccessor().invoke(value);
            } catch (IllegalAccessException | InvocationTargetException x) {
                throw new AssertionError(x);
            }
            return inner.handle(innerValue);
        };
        return new ParameterSetStrategy(h, List.of(paramName));
    }

    @NonNull
    private static ParameterReceiver.NamedAcceptor1 forEnum(@NonNull Class<?> k, @NonNull String name) {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.

        ParameterReceiver.Acceptor1 h = (@Nullable Object value) -> {
            if (value == null) {
                return (@NonNull ParameterReceiver ps) -> {
                    checkNotNull(ps); // Would be for Lombok. But Lombok don't look into lambdas.
                    ps.receiveNull(name, int.class);
                };
            }
            if (!k.isInstance(value)) throw new ParameterReceiver.IllegalValueException();
            var value2 = (Enum<?>) value;
            return (@NonNull ParameterReceiver ps) -> {
                checkNotNull(ps); // Would be for Lombok. But Lombok don't look into lambdas.
                ps.receive(name, value2.ordinal());
            };
        };
        return new ParameterSetStrategy(h, List.of(name));
    }

    @NonNull
    private static ParameterReceiver.NamedAcceptor1 forOptional(@NonNull Class<?> k, @NonNull String name, boolean flat)
            throws BadImplementationException
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.

        var in = forClass(k, name, flat);
        ParameterReceiver.Acceptor1 h = (@Nullable Object value) -> {
            if (value == null) return in.handle(null);
            if (!(value instanceof Optional<?> opt)) throw new ParameterReceiver.IllegalValueException();
            return in.handle(opt.isEmpty() ? null : k.cast(opt.get()));
        };
        return new ParameterSetStrategy(h, in.paramNames());
    }

    @NonNull
    private static <E> ParameterReceiver.NamedAcceptor1 forClass(@NonNull Class<E> k, @NonNull String name, boolean flat)
            throws BadImplementationException
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.

        if (k.isRecord()) return forRecord(k.asSubclass(Record.class), name, flat);
        if (flat) throw new UnsupportedOperationException();
        if (k.isEnum()) return forEnum(k.asSubclass(Enum.class), name);

        // TODO: Check if k is acceptable.

        Predicate<Object> pred = k.isPrimitive() ? WrapperClass.wrap(k)::isInstance : v -> v == null || k.isInstance(v);

        ParameterReceiver.Acceptor1 handler = (@Nullable Object value) -> {
            if (!pred.test(value)) throw new ParameterReceiver.IllegalValueException();
            var value2 = WrapperClass.wrap(k).cast(value);
            return (@NonNull ParameterReceiver ps) -> {
                checkNotNull(ps); // Would be for Lombok. But Lombok don't look into lambdas.
                if (value2 == null) {
                    ps.receiveNull(name, k);
                } else {
                    ps.receive(name, value2);
                }
            };
        };

        return new ParameterSetStrategy(handler, List.of(name));
    }

    @NonNull
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    private static ParameterReceiver.NamedAcceptor1 makeStrategy(@NonNull Type type, @NonNull String name, boolean flat)
            throws BadImplementationException
    {
        checkNotNull(type); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.

        if (type instanceof Class<?> kk) {
            return forClass(kk, name, flat);
        }
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            var ptt = pt.getActualTypeArguments();
            if (ptt.length == 1 && ptt[0] instanceof Class<?> ok) return forOptional(ok, name, flat);
        }
        throw new BadImplementationException("Unnaceptable type.", type);
    }

    @NonNull
    private static ParameterReceiver.NamedAcceptor1 makeStrategy(@NonNull Parameter p) throws BadImplementationException {
        checkNotNull(p); // Check recognized by lombok.
        return makeStrategy(p.getParameterizedType(), p.getName(), p.isAnnotationPresent(Flat.class));
    }

    /// Builds the parameter-binding strategy for an annotated interface method, one sub-strategy
    /// per method parameter, merged into a single [ParameterSetStrategy].
    ///
    /// @param m The method to build a strategy for; must not be `null`.
    /// @return A [ParameterSetStrategy] able to bind an argument array matching `m`'s parameters.
    /// @throws BadImplementationException If any parameter of `m` cannot be mapped to a valid
    ///         SQL parameter-binding strategy.
    /// @throws IllegalArgumentException If `m` is `null`.
    @NonNull
    @SuppressWarnings("Convert2Lambda") // Lombok won't insert code to handle @NonNull inside a lambda, but an anonymous class is ok.
    public static ParameterReceiver.NamedAcceptor1 makeStrategy(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.

        var pp = m.getParameters();
        var all = new ArrayList<ParameterReceiver.NamedAcceptor1>(pp.length);
        for (var p : pp) {
            all.add(makeStrategy(p));
        }

        ParameterReceiver.Acceptor1 h = (@Nullable Object value) -> {
            if (!(value instanceof Object[] array) || array.length != all.size()) { // Implicit null check with instanceof.
                throw new ParameterReceiver.IllegalValueException();
            }
            var acceptors = new ArrayList<ParameterReceiver.Acceptor2>(array.length);
            var i = 0;
            for (var each : all) {
                acceptors.add(each.handle(array[i]));
                i++;
            }
            return new ParameterReceiver.Acceptor2() {
                /// {@inheritDoc}
                @Override
                public void accept(@NonNull ParameterReceiver ps) throws SQLException {
                    for (var each : acceptors) {
                        each.accept(ps);
                    }
                }
            };
        };

        var names = all.stream().map(ParameterReceiver.NamedAcceptor1::paramNames).flatMap(List::stream).toList();
        return new ParameterSetStrategy(h, names);
    }

    private static void checkAccessible(@NonNull Class<? extends Record> k) throws BadImplementationException {
        checkNotNull(k); // Check recognized by lombok.
        if (!Modifier.isPublic(k.getModifiers()) || !k.getModule().isExported(k.getPackageName(), ParameterSetStrategy.class.getModule())) {
            throw new BadImplementationException("Record type must be public and its package must be exported.", k);
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}