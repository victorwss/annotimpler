package ninja.javahacker.annotimpler.jpa;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Parameter;
import java.util.Date;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module jakarta.persistence;
import module java.base;

/// Implementation of the [ExtendedTypedQuery] interface that delegates to some other [TypedQuery].
/// @author Victor Williams Stafusa da Silva
@SuppressFBWarnings(
        value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
        justification = "Several 'return this;' to preserve the return type as SpecialTypedQuery<X>"
)
@PackagePrivate
class SpecialTypedQuery<X> implements ExtendedTypedQuery<X> {

    /// The wrapped typed query, delegated to for the methods listed in [DelegatedParts].
    @Delegate(types = DelegatedParts.class)
    private final TypedQuery<X> delegate;

    /// Creates a `SpecialTypedQuery` wrapping the given typed query.
    /// @param query The typed query to delegate to.
    /// @throws IllegalArgumentException If `query` is `null`.
    public SpecialTypedQuery(@NonNull TypedQuery<X> query) {
        this.delegate = query;
    }

    /// {@inheritDoc}
    @Override
    public X getSingleResultOrNull() {
        return delegate.getSingleResultOrNull();
    }

    /// {@inheritDoc}
    @Override
    public List<X> getResultList() {
        return delegate.getResultList();
    }

    /// {@inheritDoc}
    @Override
    public Stream<X> getResultStream() {
        return delegate.getResultStream();
    }

    /// {@inheritDoc}
    @Override
    public X getSingleResult() {
        return delegate.getSingleResult();
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setMaxResults(int maxResults) {
        delegate.setMaxResults(maxResults);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setFirstResult(int startPosition) {
        delegate.setFirstResult(startPosition);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public <T> SpecialTypedQuery<X> setParameter(Parameter<T> param, T value) {
        delegate.setParameter(param, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setParameter(String name, Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setParameter(int position, Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    public SpecialTypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setTimeout(Integer timeout) {
        delegate.setTimeout(timeout);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setCacheStoreMode(CacheStoreMode mode) {
        delegate.setCacheStoreMode(mode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    public SpecialTypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode mode) {
        delegate.setCacheRetrieveMode(mode);
        return this;
    }

    /// Exists only to tell lombok which methods should be delegated.
    private static interface DelegatedParts {

        /// Don't care.
        /// @return Don't care.
        public int executeUpdate();

        /// Don't care.
        /// @return Don't care.
        public int getMaxResults();

        /// Don't care.
        /// @return Don't care.
        public int getFirstResult();

        /// Don't care.
        /// @return Don't care.
        public Integer getTimeout();

        /// Don't care.
        /// @return Don't care.
        public CacheStoreMode getCacheStoreMode();

        /// Don't care.
        /// @return Don't care.
        public CacheRetrieveMode getCacheRetrieveMode();

        /// Don't care.
        /// @return Don't care.
        public Map<String, Object> getHints();

        /// Don't care.
        /// @return Don't care.
        public Set<Parameter<?>> getParameters();

        /// Don't care.
        /// @param name Don't care.
        /// @return Don't care.
        public Parameter<?> getParameter(String name);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param name Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        public <T> Parameter<T> getParameter(String name, Class<T> type);

        /// Don't care.
        /// @param position Don't care.
        /// @return Don't care.
        public Parameter<?> getParameter(int position);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param position Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        public <T> Parameter<T> getParameter(int position, Class<T> type);

        /// Don't care.
        /// @param param Don't care.
        /// @return Don't care.
        public boolean isBound(Parameter<?> param);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param param Don't care.
        /// @return Don't care.
        public <T> T getParameterValue(Parameter<T> param);

        /// Don't care.
        /// @param name Don't care.
        /// @return Don't care.
        public Object getParameterValue(String name);

        /// Don't care.
        /// @param position Don't care.
        /// @return Don't care.
        public Object getParameterValue(int position);

        /// Don't care.
        /// @return Don't care.
        public FlushModeType getFlushMode();

        /// Don't care.
        /// @return Don't care.
        public LockModeType getLockMode();

        /// Don't care.
        /// @param <X> Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        public <X> X unwrap(Class<X> type);
    }
}