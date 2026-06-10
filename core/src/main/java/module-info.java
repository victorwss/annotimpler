/// Core framework for annotation-driven interface implementation.
///
/// This module provides the infrastructure for implementing Java interfaces dynamically at
/// runtime using annotations. Method behaviors are specified by placing annotations on
/// interface methods, where each annotation type is itself annotated with
/// [ninja.javahacker.annotimpler.core.ImplementedBy] to designate the
/// [ninja.javahacker.annotimpler.core.Implementation] class responsible for handling it.
///
/// The main entry point is [ninja.javahacker.annotimpler.core.AnnotationsImplementor].
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.core {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;

    exports ninja.javahacker.annotimpler.core;
}