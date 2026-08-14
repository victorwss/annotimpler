package ninja.javahacker.annotimpler.jpa;

import edu.umd.cs.findbugs.annotations.Nullable;
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
@SuppressWarnings({
    "PMD.TooManyMethods", // Unavoidable in order to change the return type of many inherited methods.
    "PMD.ReplaceJavaUtilCalendar", // Calendar is only used in deprecated methods that are deprecated precisely because they use Calendar.
    "PMD.ReplaceJavaUtilDate" // Date is only used in deprecated methods that are deprecated precisely because they use Date.
})
@PackagePrivate
final class SpecialTypedQuery<X> implements ExtendedTypedQuery<X> {

    /// The wrapped typed query, delegated to for the methods listed in [DelegatedParts].
    @Delegate(types = DelegatedParts.class)
    @NonNull
    private final TypedQuery<X> delegate;

    /// Creates a `SpecialTypedQuery` wrapping the given typed query.
    /// @param query The typed query to delegate to; must not be `null`.
    /// @throws IllegalArgumentException If `query` is `null`.
    public SpecialTypedQuery(@NonNull TypedQuery<X> query) {
        this.delegate = query;
    }

    /// {@inheritDoc}
    @Override
    @Nullable
    public X getSingleResultOrNull() {
        return delegate.getSingleResultOrNull();
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public List<X> getResultList() {
        return delegate.getResultList();
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public Stream<X> getResultStream() {
        return delegate.getResultStream();
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public X getSingleResult() {
        return delegate.getSingleResult();
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setMaxResults(int maxResults) {
        delegate.setMaxResults(maxResults);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setFirstResult(int startPosition) {
        delegate.setFirstResult(startPosition);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setHint(@NonNull String hintName, @NonNull Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public <T> SpecialTypedQuery<X> setParameter(@NonNull Parameter<T> param, @Nullable T value) {
        delegate.setParameter(param, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(
            @NonNull Parameter<Calendar> param,
            @Nullable Calendar value,
            @NonNull TemporalType temporalType)
    {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(
            @NonNull Parameter<Date> param,
            @Nullable Date value,
            @NonNull TemporalType temporalType)
    {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(@NonNull String name, @Nullable Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(@NonNull String name, @Nullable Calendar value, @NonNull TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(@NonNull String name, @Nullable Date value, @NonNull TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(int position, @Nullable Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(int position, @Nullable Calendar value, @NonNull TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Deprecated
    @Override
    @NonNull
    public SpecialTypedQuery<X> setParameter(int position, @Nullable Date value, @NonNull TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setFlushMode(@NonNull FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setLockMode(@NonNull LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setTimeout(@Nullable Integer timeout) {
        delegate.setTimeout(timeout);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setCacheStoreMode(@NonNull CacheStoreMode mode) {
        delegate.setCacheStoreMode(mode);
        return this;
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public SpecialTypedQuery<X> setCacheRetrieveMode(@NonNull CacheRetrieveMode mode) {
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
        @Nullable
        public Integer getTimeout();

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public CacheStoreMode getCacheStoreMode();

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public CacheRetrieveMode getCacheRetrieveMode();

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public Map<String, Object> getHints();

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public Set<Parameter<?>> getParameters();

        /// Don't care.
        /// @param name Don't care.
        /// @return Don't care.
        @NonNull
        public Parameter<?> getParameter(@NonNull String name);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param name Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        @NonNull
        public <T> Parameter<T> getParameter(@NonNull String name, @NonNull Class<T> type);

        /// Don't care.
        /// @param position Don't care.
        /// @return Don't care.
        @NonNull
        public Parameter<?> getParameter(int position);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param position Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        @NonNull
        public <T> Parameter<T> getParameter(int position, @NonNull Class<T> type);

        /// Don't care.
        /// @param param Don't care.
        /// @return Don't care.
        public boolean isBound(@NonNull Parameter<?> param);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param param Don't care.
        /// @return Don't care.
        @Nullable
        public <T> T getParameterValue(@NonNull Parameter<T> param);

        /// Don't care.
        /// @param name Don't care.
        /// @return Don't care.
        @Nullable
        public Object getParameterValue(@NonNull String name);

        /// Don't care.
        /// @param position Don't care.
        /// @return Don't care.
        @Nullable
        public Object getParameterValue(int position);

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public FlushModeType getFlushMode();

        /// Don't care.
        /// @return Don't care.
        @Nullable
        public LockModeType getLockMode();

        /// Don't care.
        /// @param <X> Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        @NonNull
        public <X> X unwrap(@NonNull Class<X> type);
    }
}