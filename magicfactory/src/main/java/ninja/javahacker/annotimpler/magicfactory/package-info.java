/// Provides utilities for reflectively discovering and invoking a *creator* — a constructor,
/// factory method, or constant field — for a given class.
///
/// The central abstraction is [MagicFactory], which inspects a class at construction time,
/// selects an appropriate creator following a well-defined priority order, and then allows
/// repeated instantiation through [MagicFactory#create(Object...)].
///
/// The [Creator] annotation can be placed on a constructor, static factory method, or static
/// field to explicitly designate the preferred creator. When no such annotation is present,
/// `MagicFactory` falls back to heuristics: single enum constants, canonical record
/// constructors, single-constructor classes, and default (no-arg) constructors.
///
/// Supporting utilities include:
/// - [MethodWrapper] — a unified reflective wrapper over constructors, methods, and fields
/// - [Methods] — predicate and invocation helpers for `java.lang.reflect.Method`
/// - [NameDictionary] — caches per-class type-name disambiguation for readable signatures
/// - [TypeName] — formats `java.lang.reflect.Type` values as human-readable strings
/// - [WrapperClass] — maps primitive types to their wrapper counterparts and vice-versa
package ninja.javahacker.annotimpler.magicfactory;