package ninja.javahacker.annotimpler.magicfactory;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class TypeName {

    private TypeName() {
        throw new UnsupportedOperationException();
    }

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
            case TypeVariable<?> tv -> sb.append(tv.getName());
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
            default -> throw new AssertionError();
        }
    }

    @NonNull
    public static String of(@NonNull Type what, @NonNull Set<? extends Class<?>> fullNameNeeded) {
        var sb = new StringBuilder(50);
        formatType(what, fullNameNeeded, sb);
        return sb.toString();
    }

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
