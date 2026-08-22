/// Backend-agnostic transaction abstraction.
///
/// This module provides [ninja.javahacker.transaction.Transactor], a reusable mechanism to wrap
/// an object's method calls in transactions: each top-level call commits on success or rolls back
/// on failure, while nested calls transparently reuse the transaction already active on the
/// current thread. The underlying resource type (a JDBC `Connection`, a JPA `EntityManager`, or
/// anything else) is a type parameter, so the same `Transactor` implementation works regardless
/// of the persistence technology; only a [ninja.javahacker.transaction.Transactor.TransactionFactory]
/// and a [ninja.javahacker.transaction.Transactor.Transaction] implementation need to be supplied
/// for each concrete backend.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.transaction {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;

    exports ninja.javahacker.transaction;
}