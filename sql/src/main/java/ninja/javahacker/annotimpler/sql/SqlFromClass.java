package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Specifies the SQL string for an annotated method by delegating to a custom
/// [ninja.javahacker.annotimpler.sql.meta.SqlSupplier] implementation.
///
/// The method must also carry a SQL-operation annotation ([ExecuteSql], [GenerateSql],
/// or [QuerySql]). The supplier class is instantiated reflectively at prepare time via
/// [ninja.javahacker.annotimpler.magicfactory.MagicFactory]. If the supplier's constructor
/// accepts a single `String`, the value of [#key()] is passed to it; otherwise the
/// no-arg constructor is used.
///
/// Example:
/// ```java
/// @ExecuteSql
/// @SqlFromClass(value = MyCustomSql.class, key = "deleteOrder")
/// void deleteOrder(int id);
/// ```
@SqlSource(SupplierSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromClass {

    /// Returns the [ninja.javahacker.annotimpler.sql.meta.SqlSupplier] class to instantiate.
    ///
    /// @return The non-null supplier class.
    public Class<? extends SqlSupplier> value();

    /// Returns the key passed to the supplier's single-`String`-argument constructor,
    /// if one exists.
    ///
    /// When the key is an empty string (the default) and the supplier has a no-arg constructor,
    /// the no-arg constructor is used instead.
    ///
    /// @return The key string; defaults to an empty string.
    public String key() default "";
}
