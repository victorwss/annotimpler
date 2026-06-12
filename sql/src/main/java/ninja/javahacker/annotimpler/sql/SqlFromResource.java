package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Specifies the SQL string for an annotated method by loading it from a classpath resource.
///
/// The method must also carry a SQL-operation annotation ([ExecuteSql], [GenerateSql],
/// or [QuerySql]). The resource is always read eagerly at prepare time.
///
/// Example:
/// ```java
/// @ExecuteSql
/// @SqlFromResource(value = "/sql/delete_order.sql", fromClass = MyDao.class)
/// void deleteOrder(int id);
/// ```
@SqlSource(ResourceSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromResource {

    /// Returns the resource path passed to [Class#getResourceAsStream(String)].
    ///
    /// @return The non-empty resource path.
    public String value();

    /// Returns the class used to locate the resource.
    ///
    /// When set to `void.class` (the default), the declaring class of the annotated
    /// method is used instead.
    ///
    /// @return The anchor class for resource lookup; defaults to `void.class`.
    public Class<?> fromClass() default void.class;

    /// Returns the character encoding used to decode the resource content.
    ///
    /// @return The charset spec class; defaults to [CharsetSpec.Utf8].
    public Class<? extends CharsetSpec> encoding() default CharsetSpec.Utf8.class;
}