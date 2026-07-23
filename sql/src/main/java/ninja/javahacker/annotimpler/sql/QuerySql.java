package ninja.javahacker.annotimpler.sql;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Marks a method as a `SELECT` query that maps results to Java types.
///
/// The method must also carry a SQL-source annotation ([Sql], [SqlFromFile],
/// [SqlFromResource], [SqlFromUrl], or [SqlFromClass]).
///
/// Supported return types:
/// - Bare scalar type (e.g., `String`, `int`) — returns the first result or
///   `null`/zero if no rows.
/// - [Optional]`<T>` — returns the first result wrapped, or empty.
/// - [OptionalInt], [OptionalLong], [OptionalDouble] — primitive optional variants.
/// - [List]`<T>` — returns all result rows.
/// - A `record` type — each row is mapped to the record's components.
///
/// Example:
/// ```java
/// @QuerySql
/// @Sql("SELECT name FROM users WHERE id = :id")
/// Optional<String> findName(int id);
/// ```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(QuerySqlImplementation.class)
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public @interface QuerySql {

    /// Specifies the result-set column indices (1-based) to map to the fields of a
    /// `record` return type.
    ///
    /// When empty (the default), columns are mapped to record components in the order they
    /// appear in the result set.  When non-empty, the array length must equal the number of
    /// record components, and each element identifies the column index for the corresponding
    /// component.
    ///
    /// @return An array of 1-based column indices, or an empty array for sequential mapping.
    public int[] fields() default {};

    /// Whether to validate that the method's parameter names match the named parameters in
    /// the SQL string at prepare time.
    ///
    /// @return `true` to enable validation (the default), `false` to skip it.
    public boolean validate() default true;
}
