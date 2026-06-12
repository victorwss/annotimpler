/// Contains the [ninja.javahacker.annotimpler.sql.meta.SqlFactory] implementations that back the
/// SQL-source annotations ([ninja.javahacker.annotimpler.sql.Sql], [ninja.javahacker.annotimpler.sql.SqlFromFile],
/// [ninja.javahacker.annotimpler.sql.SqlFromResource], [ninja.javahacker.annotimpler.sql.SqlFromUrl],
/// [ninja.javahacker.annotimpler.sql.SqlFromClass]).
/// Each factory reads metadata from the annotation present on the target method and returns a
/// [ninja.javahacker.annotimpler.sql.meta.SqlSupplier] that delivers the SQL string according
/// to the configured reading policy.
package ninja.javahacker.annotimpler.sql.sqlfactories;