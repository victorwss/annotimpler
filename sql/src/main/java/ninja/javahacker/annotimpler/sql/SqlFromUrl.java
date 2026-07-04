package ninja.javahacker.annotimpler.sql;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Specifies the SQL string for an annotated method by downloading it from an HTTP/HTTPS URL.
///
/// The method must also carry a SQL-operation annotation ([ExecuteSql], [GenerateSql],
/// or [QuerySql]). The download strategy is controlled by [#policy()]. Character
/// encoding is auto-detected from the HTTP `Content-Type` response header when
/// [#getEncodingFromHeaders()] is `true`; the [#fallbackEncoding()] is used
/// when the header is absent or detection is disabled.
///
/// Example:
/// ```java
/// @ExecuteSql
/// @SqlFromUrl(value = "https://example.com/sql/delete_order.sql",
///             policy = ReadPolicy.ON_STARTUP)
/// void deleteOrder(int id);
/// ```
@SqlSource(UrlSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public @interface SqlFromUrl {

    /// Returns the URL from which the SQL string is downloaded.
    ///
    /// @return The non-empty URL string.
    public String value();

    /// Returns the strategy used to download the SQL.
    ///
    /// @return The read policy; defaults to [ReadPolicy#EVERY_TIME].
    public ReadPolicy policy() default ReadPolicy.EVERY_TIME;

    /// Whether to detect the character encoding from the HTTP `Content-Type` response header.
    ///
    /// When `true` (the default) and the header contains a `charset` parameter,
    /// that charset is used.  Otherwise, [#fallbackEncoding()] is used.
    ///
    /// @return `true` to use the encoding from the response header, `false` to
    ///         always use the fallback encoding.
    public boolean getEncodingFromHeaders() default true;

    /// Returns the fallback character encoding used when the HTTP response headers do not
    /// specify a charset, or when [#getEncodingFromHeaders()] is `false`.
    ///
    /// @return The fallback charset spec class; defaults to [CharsetSpec.Utf8].
    public Class<? extends CharsetSpec> fallbackEncoding() default CharsetSpec.Utf8.class;
}
