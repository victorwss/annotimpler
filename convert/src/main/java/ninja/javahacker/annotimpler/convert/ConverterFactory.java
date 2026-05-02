package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface ConverterFactory {

    @NonNull
    public static final StdConverterFactory STD = StdConverterFactory.INSTANCE;

    @NonNull
    public Converter<?> get(@NonNull Type t) throws UnavailableConverterException;

    @NonNull
    @SuppressWarnings("unchecked")
    public default <E> Converter<E> getOf(@NonNull Class<E> klass) throws UnavailableConverterException {
        return (Converter<E>) get(klass);
    }

    @NonNull
    public default <T extends Record> T mapToRecord(@NonNull Map<String, ?> map, @NonNull Class<T> recordClass)
            throws MagicFactory.CreatorSelectionException,
            MagicFactory.CreationException,
            UnavailableConverterException,
            ConvertionException
    {
        // Select the best constructor or method and look onto its parameters.
        var exec = MagicFactory.of(recordClass);
        var parameters = exec.getParameters();

        // Check if the constructor or method parameters match those from the map.
        Set<String> execKeys = parameters.stream().map(Parameter::getName).collect(Collectors.toSet());
        if (!Objects.equals(map.keySet(), execKeys)) {
            throw new ConvertionException("Map keys mismatch.", Map.class, recordClass);
        }

        // Prepare the arguments for the constructor or method.
        var args = new ArrayList<Object>(parameters.size());
        for (var param : parameters) {
            var paramName = param.getName(); //camelCaseToSnakeCase(param.getName());
            var paramType = param.getParameterizedType();
            var value = this.get(paramType).fromObj(map.get(paramName)).orElse(null);
            args.add(value);
        }

        // Execute the constructor or method with the arguments.
        return exec.create(args.toArray());
    }

    /*@NonNull
    private static String camelCaseToSnakeCase(@NonNull String paramName) {
        checkNotNull(paramName);
        var tam = paramName.length();
        var sb = new StringBuilder(tam);
        for (var i = 0; i < tam; i++) {
            var c = paramName.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                if (i != 0) sb.append('_');
                sb.append((char) (c + 32));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }*/
}