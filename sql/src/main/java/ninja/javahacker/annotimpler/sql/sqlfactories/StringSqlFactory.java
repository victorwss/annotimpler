package ninja.javahacker.annotimpler.sql.sqlfactories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Singleton [SqlFactory] that extracts the SQL string directly from the [Sql#value()] attribute of a
/// [Sql]-annotated method.
/// The SQL string is captured at prepare time and returned on every call.
@SuppressFBWarnings({"ENMI_ONE_ENUM_VALUE", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY"})
public enum StringSqlFactory implements SqlFactory {

    /// The sole instance of this factory.
    INSTANCE;

    /// Returns a [SqlSupplier] that always yields the inline SQL string declared in the [Sql] annotation on `m`.
    ///
    /// @param m The method carrying the [Sql] annotation.
    /// @return A [SqlSupplier] that supplies the inline SQL string.
    /// @throws UnsupportedOperationException If `m` has no [Sql] annotation.
    /// @throws IllegalArgumentException If `m` is `null`.
    @Override
    public SqlSupplier prepare(@NonNull Method m) {
        var anno = m.getAnnotation(Sql.class);
        if (anno == null) throw new UnsupportedOperationException();
        var v = anno.value();
        return () -> v;
    }
}
