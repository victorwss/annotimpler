package ninja.javahacker.annotimpler.core;

import module java.base;

/// Meta-annotation that designates an [Implementation] class for an annotation type.
///
/// When an annotation type `A` is annotated with `@ImplementedBy(Foo.class)`, placing `@A`
/// on an interface method tells [AnnotationsImplementor] to use `Foo` to provide that
/// method's behavior. [AnnotationsImplementor] instantiates `Foo` via `MagicFactory`.
/// If `Foo`'s creator takes a single [PropertyBag] argument, the current property bag is
/// passed to it; otherwise it is invoked with no arguments. [AnnotationsImplementor] then
/// calls [Implementation#prepare] to obtain the [CallContext] for the annotated method.
///
/// ## Example
///
/// ```java
/// @Target(ElementType.METHOD)
/// @Retention(RetentionPolicy.RUNTIME)
/// @ImplementedBy(GetterImplementation.class)
/// public @interface Getter {
///     String field();
/// }
/// ```
///
/// @see Implementation
/// @see AnnotationsImplementor
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImplementedBy {
    /// The [Implementation] class responsible for handling methods annotated with
    /// the annotation type that carries this `@ImplementedBy` annotation.
    ///
    /// @return The implementation class.
    public Class<? extends Implementation> value();
}
