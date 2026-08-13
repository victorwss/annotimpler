package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// Denotes something that may be available or may be failed with some exception.
/// @param <T> The type of the object representing a success.
/// @author Victor Williams Stafusa da Silva
public sealed interface Maybe<T> permits Maybe.MaybeSuccess, Maybe.MaybeFailure {

    /// True if this represents a success.
    /// @return if this represents a success.
    public default boolean isSuccess() {
        return this instanceof MaybeSuccess;
    }

    /// Returns an [Optional] containing the failure or empty if it wasn't a failure.
    /// @return An [Optional] containing the failure or empty if it wasn't a failure.
    @NonNull
    public default Optional<Throwable> failure() {
        return Optional.empty();
    }

    /// Produces a failure instance containing the given error.
    /// @param <T> The type of the success instance, even if it wasn't a success afterall, needed to typecheck correctly.
    /// @param oops The error producing the failure.
    /// @return An instance of the `Maybe` representing a failure containing the given entry.
    /// @throws IllegalArgumentException If `oops` is `null`.
    @NonNull
    public static <T> Maybe<T> failure(@NonNull Throwable oops) {
        return new MaybeFailure<>(oops);
    }

    /// Returns an [Optional] containing the resulting success or empty if it wasn't a success.
    /// @return An [Optional] containing the resulting success or empty if it wasn't a success.
    @NonNull
    public default Optional<T> success() {
        return Optional.empty();
    }

    /// Produces a success instance containing the given entry.
    /// @param <T> The type of the success instance.
    /// @param entry The success instance.
    /// @return An instance of the `Maybe` representing a success containing the given entry.
    /// @throws IllegalArgumentException If `entry` is `null`.
    @NonNull
    public static <T> Maybe<T> success(@NonNull T entry) {
        return new MaybeSuccess<>(entry);
    }

    /// Returns the content, be it a success instance or an error.
    /// @return The content, whatever it is.
    @NonNull
    public default Object content() {
        return isSuccess() ? success().get() : failure().get();
    }

    /// Wrap a `Supplier<T>` that could throw an exception into one that gives off a `Maybe` instance instead.
    /// @param <T> The given `Supplier`'s type.
    /// @param inner The given `Supplier`'s to be wrapped.
    /// @return The given `Supplier` wrapped into a `Supplier` that produces a `Maybe` instance.
    /// @throws IllegalArgumentException If `inner` is `null`.
    @NonNull
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static <T> Supplier<Maybe<T>> wrap(@NonNull Supplier<T> inner) {
        return () -> {
            try {
                return success(inner.get());
            } catch (Throwable oops) {
                return failure(oops);
            }
        };
    }

    /// Flatten out a `Maybe` of another `Maybe`.
    /// @param <T> The type of the inner `Maybe`.
    /// @param tooDeep The two-layered `Maybe` to be flatten out.
    /// @return A `Maybe` instance with only one layer.
    /// @throws IllegalArgumentException If `tooDeep` is `null`.
    @NonNull
    public static <T> Maybe<T> flatten(@NonNull Maybe<Maybe<T>> tooDeep) {
        return !tooDeep.isSuccess() ? failure(tooDeep.failure().get()) : tooDeep.success().get();
    }

    /// Implementation of [Maybe] in case of success.
    /// @param <T> The type of the object representing a success.
    /// @param entry The object representing a success.
    /// @author Victor Williams Stafusa da Silva
    @PackagePrivate
    final record MaybeSuccess<T>(@NonNull T entry) implements Maybe<T> {

        /// Creates a `MaybeSuccess` with the given success entry.
        /// @param entry The object representing a success.
        /// @throws IllegalArgumentException If `entry` is `null`.
        public MaybeSuccess {}

        /// {@inheritDoc}
        @NonNull
        @Override
        public Optional<T> success() {
            return Optional.of(entry);
        }
    }

    /// Implementation of [Maybe] in case of failure.
    /// @param <T> The type of the object representing a success. Irrelevant in case of a failure.
    /// @param oops The failure.
    /// @author Victor Williams Stafusa da Silva
    @PackagePrivate
    final record MaybeFailure<T>(@NonNull Throwable oops) implements Maybe<T> {

        /// Creates a `MaybeFailure` with the given error.
        /// @param oops The failure.
        /// @throws IllegalArgumentException If `oops` is `null`.
        public MaybeFailure {}

        /// {@inheritDoc}
        @NonNull
        @Override
        public Optional<Throwable> failure() {
            return Optional.of(oops);
        }
    }
}