package ninja.javahacker.annotimpler.jpa;

import module jakarta.persistence;
import module java.base;
import jakarta.persistence.Parameter;
import java.util.Date;
import lombok.NonNull;

/**
 * Extends the {@link TypedQuery} interface adding several useful methods.
 * @param <X> The generic type of the {@link TypedQuery}.
 * @author Victor Williams Stafusa da Silva
 */
public interface ExtendedTypedQuery<X> extends TypedQuery<X> {

    /// Gets a single line as a result and wraps it inside an [Optional]. If there is no result, an empty [Optional]
    /// is returned instead.
    /// @return An [Optional] containing the result or an empty [Optional] if there is no result.
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
    public static <X> ExtendedTypedQuery<X> wrap(@NonNull TypedQuery<X> query) throws IllegalArgumentException {
        return query instanceof ExtendedTypedQuery<X> q ? q : new SpecialTypedQuery<>(query);
    }

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setMaxResults(int maxResults) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setFirstResult(int startPosition) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setHint(String hintName, Object value) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public <T extends Object> ExtendedTypedQuery<X> setParameter(Parameter<T> param, T value) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(
            Parameter<Calendar> param,
            Calendar value,
            TemporalType temporalType)
            throws IllegalArgumentException;

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setParameter(String name, Object value) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setParameter(int position, Object value) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType);

    /// {@inheritDoc}
    @Deprecated
    @Override
    public ExtendedTypedQuery<X> setParameter(int position, Date value, TemporalType temporalType);

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setFlushMode(FlushModeType flushMode);

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setLockMode(LockModeType lockMode) throws IllegalArgumentException;

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode mode);

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setCacheStoreMode(CacheStoreMode mode);

    /// {@inheritDoc}
    @Override
    public ExtendedTypedQuery<X> setTimeout(Integer timeout);
}