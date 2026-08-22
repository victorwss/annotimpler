/// Backend-agnostic transaction abstraction.
///
/// Provides [Transactor], which wraps an object's method calls in transactions backed by a
/// pluggable [Transactor.TransactionFactory] and [Transactor.Transaction] pair, so the same
/// transactional-proxy mechanism can be reused across JDBC, JPA or any other resource type.
package ninja.javahacker.transaction;