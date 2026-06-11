package ninja.javahacker.annotimpler.magicfactory;

import module java.base;

/// Marks the preferred *creator* for a class — the constructor, static factory method,
/// or static field that [MagicFactory] should use to produce instances of that class.
///
/// At most one member of a given class may carry this annotation. If more than one member
/// is annotated, [MagicFactory#of(Class)] throws
/// [MagicFactory.CreatorSelectionException].
///
/// ## Rules for the annotated member
///
/// The annotated member must satisfy all of the following:
/// - It must be **public**.
/// - It must be **static** (factory method or field) or a **constructor** (which is implicitly
///   non-instance).
/// - For methods and fields, the declared return type (or field type) must be assignable to
///   the target class passed to `MagicFactory.of`.
/// - The enclosing class must be **public** and **non-abstract**.
///
/// If any rule is violated, `MagicFactory.of` throws `CreatorSelectionException` with a
/// descriptive message.
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Creator {
}
