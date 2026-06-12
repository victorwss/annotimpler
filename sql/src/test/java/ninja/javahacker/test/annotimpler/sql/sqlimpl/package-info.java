/// Tests for the SQL implementation classes [ExecuteSqlImplementation],
/// [GenerateSqlImplementation] and [QuerySqlImplementation].
///
/// The tests in this package exercise annotation-driven DAO method compilation and execution
/// against an in-memory H2 database, verifying return-type selection, parameter validation,
/// and result-set mapping for each of the three SQL operation kinds.
package ninja.javahacker.test.annotimpler.sql.sqlimpl;