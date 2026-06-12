package ninja.javahacker.annotimpler.sql;

import module java.base;

/// Marks a method parameter as a "flat" parameter whose fields are bound individually
/// as named SQL parameters.
///
/// When a parameter is annotated with `@Flat`, its accessible fields are extracted
/// and bound to the SQL named parameters using the field names. This allows a single
/// object (such as a record or a bean) to supply multiple SQL parameters.
///
/// Example:
/// ```java
/// record Product(int id, String label, BigDecimal price) {}
///
/// @ExecuteSql
/// @Sql("INSERT INTO products VALUES (:id, :label, :price)")
/// void insertProduct(@Flat Product product);
/// ```
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flat {
}
