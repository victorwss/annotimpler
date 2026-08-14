package ninja.javahacker.annotimpler.jpa;

import edu.umd.cs.findbugs.annotations.Nullable;
import jakarta.persistence.Parameter;
import java.util.Date;
import lombok.NonNull;

import module jakarta.persistence;
import module java.base;

/// Extends the [TypedQuery] interface adding several useful methods.
/// @param <X> The generic type of the [TypedQuery].
/// @author Victor Williams Stafusa da Silva
@SuppressWarnings({
    "PMD.ReplaceJavaUtilCalendar", // Calendar is only used in deprecated methods that are deprecated precisely because they use Calendar.
    "PMD.ReplaceJavaUtilDate" // Date is only used in deprecated methods that are deprecated precisely because they use Date.
})
public interface ExtendedTypedQuery<X> extends TypedQuery<X> {

    /// Gets a single line as a result and wraps it inside an [Optional].
    /// If there is no result, an empty [Optional] is returned instead.
    /// @return An [Optional] containing the result or an empty [Optional] if there is no result.
    @NonNull
    public default Optional<X> getOptionalResult() {
        try {
            return Optional.of(getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /// Produces an instance of an [ExtendedTypedQuery] from a common [TypedQuery].
    /// If the given instance is already an [ExtendedTypedQuery], simply returns it unchanged.
    /// @param <X> The generic type of the [TypedQuery].
    /// @param query The [TypedQuery] that should be decorated as an [ExtendedTypedQuery].
    /// @return An [ExtendedTypedQuery] corresponding to a decorator of the given [TypedQuery].
    /// @throws IllegalArgumentException If `query` is `null`.
    @NonNull
    public static <X> ExtendedTypedQuery<X> wrap(@NonNull TypedQuery<X> query) {
        return query instanceof ExtendedTypedQuery<X> q ? q : new SpecialTypedQuery<>(query);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setMaxResults(int maxResults);

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setFirstResult(int startPosition);

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setHint(@NonNull String hintName, @NonNull Object value);

    /// {@inheritDoc}
    @NonNull
    @Override
    public <T extends Object> ExtendedTypedQuery<X> setParameter(@NonNull Parameter<T> param, @Nullable T value);

    /// {@inheritDoc}
    @Deprecated
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setParameter(
            @NonNull Parameter<Calendar> param,
            @Nullable Calendar value,
            @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @Deprecated
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setParameter(
            @NonNull Parameter<Date> param,
            @Nullable Date value,
            @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setParameter(@NonNull String name, @Nullable Object value);

    /// {@inheritDoc}
    @Deprecated
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setParameter(@NonNull String name, @Nullable Calendar value, @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @Deprecated
    @NonNull
    @Override
    public ExtendedTypedQuery<X> setParameter(@NonNull String name, @Nullable Date value, @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setParameter(int position, @Nullable Object value);

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setParameter(int position, @Nullable Calendar value, @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setParameter(int position, @Nullable Date value, @NonNull TemporalType temporalType);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setFlushMode(@NonNull FlushModeType flushMode);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setLockMode(@NonNull LockModeType lockMode);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setCacheRetrieveMode(@NonNull CacheRetrieveMode mode);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setCacheStoreMode(@NonNull CacheStoreMode mode);

    /// {@inheritDoc}
    @Override
    @NonNull
    public ExtendedTypedQuery<X> setTimeout(@Nullable Integer timeout);
}