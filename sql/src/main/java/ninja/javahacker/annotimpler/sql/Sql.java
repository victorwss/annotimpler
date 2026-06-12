package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Specifies the SQL string for an annotated method as an inline literal.
///
/// The SQL text is embedded directly in the annotation value and is used as-is at runtime.
/// This annotation must appear together with exactly one SQL-operation annotation
/// ([ExecuteSql], [GenerateSql], or [QuerySql]).
///
/// Example:
/// ```java
/// @ExecuteSql
/// @Sql("DELETE FROM orders WHERE id = :id")
/// void deleteOrder(int id);
/// ```
@SqlSource(StringSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql {

    /// Returns the SQL string to use for the annotated method.
    ///
    /// @return The non-empty SQL text.
    public String value();
}
