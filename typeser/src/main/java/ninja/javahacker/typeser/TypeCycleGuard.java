package ninja.javahacker.typeser;

import lombok.experimental.PackagePrivate;

import module java.base;

/// Holds the per-thread bookkeeping used by [SerializableType#from(Type)] to detect cyclic type
/// graphs, which are only possible with a hand-written or maliciously-crafted [ParameterizedType],
/// [WildcardType] or [GenericArrayType] implementation, never with a real reflection-provided `Type`.
///
/// This state lives in its own top-level class (instead of directly inside [SerializableType])
/// because interfaces, unlike classes, may not declare private fields.
@PackagePrivate
final class TypeCycleGuard {

    /// Tracks, per thread, the `Type` instances whose surrogate is currently being built by an
    /// in-progress, still-unfinished call to [SerializableType#from(Type)] on that same thread.
    ///
    /// Identity (not [Object#equals(Object)]) is used to recognize a previously-seen `Type`, since
    /// a malicious implementation could otherwise define `equals` in a way that defeats the
    /// cycle check (or that itself misbehaves, e.g. by recursing back into the very type graph
    /// being serialized).
    static final ThreadLocal<Set<Type>> VISITING = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /// Not instantiable.
    /// @throws UnsupportedOperationException Always.
    private TypeCycleGuard() {
        throw new UnsupportedOperationException();
    }
}
