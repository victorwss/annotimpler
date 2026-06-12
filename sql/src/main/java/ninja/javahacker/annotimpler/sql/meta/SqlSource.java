package ninja.javahacker.annotimpler.sql.meta;

import module java.base;

/// Meta-annotation that declares the [SqlFactory] responsible for handling an SQL annotation type.
///
/// Place this annotation on another annotation type to designate that annotation as an
/// SQL-loading annotation. When [ParsedSqlSupplier#find] scans the annotations on a method,
/// it looks for exactly one annotation whose type is itself meta-annotated with `@SqlSource`,
/// reads the [value] attribute to discover the [SqlFactory] class, and then uses that factory
/// to build the [SqlSupplier] for the method.
///
/// @see SqlFactory
/// @see ParsedSqlSupplier#find
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SqlSource {

    /// Returns the [SqlFactory] class that handles the annotated annotation type.
    ///
    /// @return The [SqlFactory] implementation class; never `null`.
    public Class<? extends SqlFactory> value();
}
