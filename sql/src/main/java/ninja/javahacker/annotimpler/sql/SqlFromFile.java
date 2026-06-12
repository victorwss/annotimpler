package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Specifies the SQL string for an annotated method by loading it from a file on the filesystem.
///
/// The method must also carry a SQL-operation annotation ([ExecuteSql], [GenerateSql],
/// or [QuerySql]). The file is read using the configured [#encoding()] and the
/// reading strategy is controlled by [#policy()].
///
/// Example:
/// ```java
/// @ExecuteSql
/// @SqlFromFile(value = "sql/delete_order.sql", policy = ReadPolicy.ON_STARTUP)
/// void deleteOrder(int id);
/// ```
@SqlSource(FileSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromFile {

    /// Returns the filesystem path of the SQL file.
    ///
    /// @return The non-empty path string.
    public String value();

    /// Returns the strategy used to read the file.
    ///
    /// @return The read policy; defaults to [ReadPolicy#EVERY_TIME].
    public ReadPolicy policy() default ReadPolicy.EVERY_TIME;

    /// Returns the character encoding used to decode the file content.
    ///
    /// @return The charset spec class; defaults to [CharsetSpec.Utf8].
    public Class<? extends CharsetSpec> encoding() default CharsetSpec.Utf8.class;
}
