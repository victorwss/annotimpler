package ninja.javahacker.annotimpler.magicfactory;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class TypeName {

    private TypeName() {
        throw new UnsupportedOperationException();
    }

    public static void formatType(@NonNull Type type, @NonNull Map<Class<?>, String> mapIn, @NonNull StringBuilder sb) {
        checkNotNull(type);
        checkNotNull(sb);
        switch (type) {
            case Class<?> clazz -> {
                if (clazz.isArray()) {
                    formatType(clazz.getComponentType(), mapIn, sb);
                    sb.append("[]");
                } else {
                    var c = mapIn.getOrDefault(clazz, clazz.getSimpleName());
                    checkNotNull(c);
                    sb.append(c);
                }
            }
            case ParameterizedType paramType -> {
                formatType(paramType.getRawType(), mapIn, sb);
                sb.append("<");
                var typeArgs = paramType.getActualTypeArguments();
                for (var i = 0; i < typeArgs.length; i++) {
                    if (i > 0) sb.append(", ");
                    formatType(typeArgs[i], mapIn, sb);
                }
                sb.append(">");
            }
            case TypeVariable<?> tv -> sb.append(tv.getName());
            case GenericArrayType arrayType -> {
                formatType(arrayType.getGenericComponentType(), mapIn, sb);
                sb.append("[]");
            }
            case WildcardType wildcardType -> {
                sb.append("?");

                var upperBounds = wildcardType.getUpperBounds();
                var lowerBounds = wildcardType.getLowerBounds();
                assertTrue(upperBounds.length == 1);
                assertTrue(lowerBounds.length <= 1);

                if (upperBounds[0] != Object.class) {
                    sb.append(" extends ");
                    formatType(upperBounds[0], mapIn, sb);
                } else if (lowerBounds.length > 0) {
                    sb.append(" super ");
                    formatType(lowerBounds[0], mapIn, sb);
                }
            }
            default -> throw new AssertionError();
        }
    }

    @NonNull
    public static String of(@NonNull Type what) {
        var sb = new StringBuilder(50);
        formatType(what, Map.of(), sb);
        return sb.toString();
    }

    @Generated
    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
