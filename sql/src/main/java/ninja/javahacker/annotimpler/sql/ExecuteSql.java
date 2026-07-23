package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// Marks a method as a DML operation (such as `INSERT`, `UPDATE`, or
/// `DELETE`) that does not return generated keys.
///
/// The method must also carry a SQL-source annotation ([Sql], [SqlFromFile],
/// [SqlFromResource], [SqlFromUrl], or [SqlFromClass]).
///
/// Supported return types: `void`, `int`, `Integer`, `long`,
/// or `Long` (representing the number of rows affected).
///
/// Example:
/// ```java
/// @ExecuteSql
/// @Sql("UPDATE accounts SET balance = :balance WHERE id = :id")
/// int updateBalance(long id, BigDecimal balance);
/// ```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(ExecuteSqlImplementation.class)
public @interface ExecuteSql {

    /// Whether the operation is allowed to affect zero rows.
    ///
    /// When `false` (the default), executing the SQL and affecting zero rows throws a
    /// [SQLException].
    ///
    /// @return `true` if affecting zero rows is permitted, `false` otherwise.
    public boolean acceptsZero() default false;

    /// Whether the operation is allowed to affect more than one row.
    ///
    /// When `false` (the default), executing the SQL and affecting more than one row
    /// throws a [SQLException].
    ///
    /// @return `true` if affecting multiple rows is permitted, `false` otherwise.
    public boolean acceptsMulti() default false;

    /// Whether to validate that the method's parameter names match the named parameters in
    /// the SQL string at prepare time.
    ///
    /// @return `true` to enable validation (the default), `false` to skip it.
    public boolean validate() default true;
}