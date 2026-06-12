package ninja.javahacker.annotimpler.sql;

/// Controls when a SQL string loaded from an external source (file, URL, resource) is
/// validated against the database.
///
/// This enum is not yet used at runtime; it is reserved for a future validation feature.
public enum SqlPreValidation {

    /// No pre-validation is performed.
    NONE,

    /// Validation is performed once when the SQL source is first loaded.
    ON_LOAD,

    /// Validation is performed on every execution.
    ON_EXECUTE;
}
