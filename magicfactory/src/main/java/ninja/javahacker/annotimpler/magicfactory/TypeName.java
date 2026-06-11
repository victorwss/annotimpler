package ninja.javahacker.annotimpler.magicfactory;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// Utility class for formatting [java.lang.reflect.Type] values as human-readable strings.
///
/// The produced strings use simple class names whenever there is no ambiguity, and fall back
/// to fully qualified names when two different classes share the same simple name within the
/// context of the same class (as determined by [NameDictionary]).
///
/// This class is not instantiable.
public final class TypeName {

    private TypeName() {
        throw new UnsupportedOperationException();
    }

    /// Appends a human-readable representation of `type` to `sb`.
    ///
    /// - [Class] values use the simple name, unless they appear in `fullNameNeeded` (or are
    ///   anonymous/hidden), in which case the binary name is used.
    /// - [ParameterizedType] values format the raw type followed by angle-bracketed type
    ///   arguments.
    /// - [TypeVariable] values use the variable's name.
    /// - [GenericArrayType] values append `[]` after the component type.
    /// - [WildcardType] values append `?`, optionally with `extends` or `super` bounds.
    ///
    /// @param type          the type to format; must not be `null`
    /// @param fullNameNeeded the set of classes that require fully qualified names; must not
    ///         be `null`
    /// @param sb            the string builder to append to; must not be `null`
    /// @throws IllegalArgumentException if any parameter is `null`
    public static void formatType(@NonNull Type type, @NonNull Set<? extends Class<?>> fullNameNeeded, @NonNull StringBuilder sb) {
        switch (type) {
            case Class<?> clazz -> {
                if (clazz.isArray()) {
                    formatType(clazz.getComponentType(), fullNameNeeded, sb);
                    sb.append("[]");
                } else {
                    var c = fullNameNeeded.contains(clazz) || clazz.isAnonymousClass() || clazz.isHidden() ? clazz.getName() : clazz.getSimpleName();
                    checkNotNull(c);
                    sb.append(c);
                }
            }
            case ParameterizedType paramType -> {
                formatType(paramType.getRawType(), fullNameNeeded, sb);
                sb.append("<");
                var typeArgs = paramType.getActualTypeArguments();
                for (var i = 0; i < typeArgs.length; i++) {
                    if (i > 0) sb.append(", ");
                    formatType(typeArgs[i], fullNameNeeded, sb);
                }
                sb.append(">");
            }
            case TypeVariable<?> tv -> {
                sb.append(tv.getName());
            }
            case GenericArrayType arrayType -> {
                formatType(arrayType.getGenericComponentType(), fullNameNeeded, sb);
                sb.append("[]");
            }
            case WildcardType wildcardType -> {
                sb.append("?");

                var upperBounds = wildcardType.getUpperBounds();
                var lowerBounds = wildcardType.getLowerBounds();
                assertEquals(upperBounds.length, 1);
                assertLE(lowerBounds.length, 1);

                if (upperBounds[0] != Object.class) {
                    sb.append(" extends ");
                    formatType(upperBounds[0], fullNameNeeded, sb);
                } else if (lowerBounds.length > 0) {
                    sb.append(" super ");
                    formatType(lowerBounds[0], fullNameNeeded, sb);
                }
            }
            default -> {
                sb.append(type.getTypeName());
            }
        }
    }

    /// Returns a human-readable string representation of `what`, using simple names for
    /// classes not in `fullNameNeeded`.
    ///
    /// @param what          the type to format; must not be `null`
    /// @param fullNameNeeded the set of classes that require fully qualified names; must not
    ///         be `null`
    /// @return the formatted type string; never `null`
    /// @throws IllegalArgumentException if `what` or `fullNameNeeded` is `null`
    @NonNull
    public static String of(@NonNull Type what, @NonNull Set<? extends Class<?>> fullNameNeeded) {
        var sb = new StringBuilder(50);
        formatType(what, fullNameNeeded, sb);
        return sb.toString();
    }

    /// Returns a human-readable string representation of `what`, always using simple names.
    ///
    /// Equivalent to `of(what, Set.of())`.
    ///
    /// @param what the type to format; must not be `null`
    /// @return the formatted type string; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
    @NonNull
    public static String of(@NonNull Type what) {
        return of(what, Set.of());
    }

    @Generated
    private static void assertEquals(int a, int b) {
        if (a != b) throw new AssertionError();
    }

    @Generated
    private static void assertLE(int a, int b) {
        if (a > b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
