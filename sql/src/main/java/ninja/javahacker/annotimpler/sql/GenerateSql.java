package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// Marks a method as a DML operation that also returns auto-generated keys.
///
/// The method must also carry a SQL-source annotation ([Sql], [SqlFromFile],
/// [SqlFromResource], [SqlFromUrl], or [SqlFromClass]).
///
/// Supported return types: `int`, `Integer`, `long`, `Long`,
/// [OptionalInt], [OptionalLong], [List]`<Integer>`, or [List]`<Long>`
/// (representing the generated key or keys).
///
/// Example:
/// ```java
/// @GenerateSql
/// @Sql("INSERT INTO products (label) VALUES (:label)")
/// long insertProduct(String label);
/// ```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(GenerateSqlImplementation.class)
public @interface GenerateSql {

    /// Whether to validate that the method's parameter names match the named parameters in
    /// the SQL string at prepare time.
    ///
    /// @return `true` to enable validation (the default), `false` to skip it.
    public boolean validate() default true;
}