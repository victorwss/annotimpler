/// Provides JDBC [ninja.javahacker.annotimpler.sql.ConnectionFactory] implementations for
/// commonly used relational database systems, together with JSON serialisation support via
/// [ninja.javahacker.annotimpler.sql.conn.JsonConnector].
///
/// All concrete connectors are immutable records (or a singleton enum in the case of
/// [ninja.javahacker.annotimpler.sql.conn.SqliteMemoryConnector]).
/// Instances are customised via copy-on-write `withXxx` methods, typically starting from
/// the standard defaults returned by the static `std()` factory of each class:
///
/// ```java
/// Connection con = MySqlConnector.std()
///         .withHost("db.example.com")
///         .withDatabase("mydb")
///         .withAuth("user", "secret")
///         .get();
/// ```
///
/// Connectors can also be deserialised from JSON using
/// [ninja.javahacker.annotimpler.sql.conn.JsonConnector#read(String)].
package ninja.javahacker.annotimpler.sql.conn;