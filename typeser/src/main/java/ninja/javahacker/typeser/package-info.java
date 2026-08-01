/// Utilities for serializing `java.lang.reflect.Type` values.
///
/// Supported runtime types are `Class`, `ParameterizedType`, `WildcardType`,
/// `GenericArrayType` and `TypeVariable`. A `TypeVariable` is reconstructed by looking it up,
/// by name, among the type parameters of its reconstructed `GenericDeclaration`
/// (a `Class`, `Method` or `Constructor`), so its bounds and annotations are preserved intact.
package ninja.javahacker.typeser;