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
    /// @param type The type to format; must not be `null`.
    /// @param fullNameNeeded The set of classes that require fully qualified names; must not be `null`.
    /// @param sb The string builder to append to; must not be `null`.
    /// @throws IllegalArgumentException If any parameter is `null`.
    public static void formatType(@NonNull Type type, @NonNull Set<? extends Class<?>> fullNameNeeded, @NonNull StringBuilder sb) {
        switch (type) {
            case Class<?> clazz -> innerFormat(clazz, fullNameNeeded, sb);
            case ParameterizedType paramType -> innerFormat(paramType, fullNameNeeded, sb);
            case TypeVariable<?> tv -> innerFormat(tv, fullNameNeeded, sb);
            case GenericArrayType arrayType -> innerFormat(arrayType, fullNameNeeded, sb);
            case WildcardType wildcardType -> innerFormat(wildcardType, fullNameNeeded, sb);
            default -> innerFormatUnknown(type, fullNameNeeded, sb);
        }
    }

    private static void innerFormat(
            @NonNull Class<?> clazz,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(clazz);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);
        if (clazz.isArray()) {
            formatType(clazz.getComponentType(), fullNameNeeded, sb);
            sb.append("[]");
        } else {
            var c = fullNameNeeded.contains(clazz) || clazz.isAnonymousClass() || clazz.isHidden()
                    ? clazz.getName()
                    : clazz.getSimpleName();
            checkNotNull(c);
            sb.append(c);
        }
    }

    private static void innerFormat(
            @NonNull ParameterizedType paramType,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(paramType);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);
        formatType(paramType.getRawType(), fullNameNeeded, sb);
        sb.append('<');
        var typeArgs = paramType.getActualTypeArguments();
        for (var i = 0; i < typeArgs.length; i++) {
            if (i > 0) sb.append(", ");
            formatType(typeArgs[i], fullNameNeeded, sb);
        }
        sb.append('>');
    }

    private static void innerFormat(
            @NonNull TypeVariable<?> tv,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(tv);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);
        sb.append(tv.getName());
    }

    private static void innerFormat(
            @NonNull GenericArrayType arrayType,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(arrayType);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);
        formatType(arrayType.getGenericComponentType(), fullNameNeeded, sb);
        sb.append("[]");
    }

    private static void innerFormat(
            @NonNull WildcardType wildcardType,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(wildcardType);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);

        sb.append('?');

        var upperBounds = wildcardType.getUpperBounds();
        var lowerBounds = wildcardType.getLowerBounds();
        assertEquals(upperBounds.length, 1);
        assertLE(lowerBounds.length, 1);

        if (lowerBounds.length > 0) {
            sb.append(" super ");
            formatType(lowerBounds[0], fullNameNeeded, sb);
        } else if (upperBounds[0] != Object.class) {
            sb.append(" extends ");
            formatType(upperBounds[0], fullNameNeeded, sb);
        }
    }

    private static void innerFormatUnknown(
            @NonNull Type unknownType,
            @NonNull Set<? extends Class<?>> fullNameNeeded,
            @NonNull StringBuilder sb)
    {
        checkNotNull(unknownType);
        checkNotNull(fullNameNeeded);
        checkNotNull(sb);
        sb.append(unknownType.getTypeName());
    }

    /// Returns a human-readable string representation of `what`, using simple names for
    /// classes not in `fullNameNeeded`.
    ///
    /// @param what The type to format; must not be `null`.
    /// @param fullNameNeeded The set of classes that require fully qualified names; must not be `null`.
    /// @return The formatted type string; never `null`.
    /// @throws IllegalArgumentException If `what` or `fullNameNeeded` is `null`.
    @NonNull
    public static String nameOf(@NonNull Type what, @NonNull Set<? extends Class<?>> fullNameNeeded) {
        var sb = new StringBuilder(50);
        formatType(what, fullNameNeeded, sb);
        return sb.toString();
    }

    /// Returns a human-readable string representation of `what`, always using simple names.
    ///
    /// Equivalent to `of(what, Set.of())`.
    ///
    /// @param what The type to format; must not be `null`.
    /// @return The formatted type string; never `null`.
    /// @throws IllegalArgumentException If `what` is `null`.
    @NonNull
    public static String nameOf(@NonNull Type what) {
        return nameOf(what, Set.of());
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
