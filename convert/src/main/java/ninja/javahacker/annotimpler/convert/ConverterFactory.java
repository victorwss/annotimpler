package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

/// A functional interface for obtaining [Converter] instances for arbitrary [Type] values.
///
/// The standard implementation is accessible via [#std()]. Custom implementations can
/// be created by implementing [#get(Type)] or by extending [StdConverterFactory]
/// using [StdConverterFactory#extend(Class, Converter)].
@FunctionalInterface
public interface ConverterFactory {

    /// Returns the standard converter factory with pre-registered converters for all built-in supported types.
    ///
    /// @return The standard converter factory with pre-registered converters for all built-in supported types.
    @NonNull
    public static StdConverterFactory std() {
        return StdConverterFactory.INSTANCE;
    }

    /// Returns a [Converter] for the given type.
    ///
    /// @param t The type to obtain a converter for.
    /// @return A [Converter] capable of producing values of the given type.
    /// @throws UnavailableConverterException If no converter is available for `t`.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public Converter<?> get(@NonNull Type t) throws UnavailableConverterException;

    /// Returns a typed [Converter] for the given class, casting the result of [#get(Type)].
    ///
    /// @param <E> The target type.
    /// @param klass The class to obtain a converter for.
    /// @return A [Converter] typed to `E`.
    /// @throws UnavailableConverterException If no converter is available for `klass`.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    @SuppressWarnings("unchecked")
    public default <E> Converter<E> getOf(@NonNull Class<E> klass) throws UnavailableConverterException {
        return (Converter<E>) get(klass);
    }

    /// Maps a string-keyed map of raw values to an instance of the given record class.
    ///
    /// Each map entry is converted using the appropriate converter for the corresponding record field's type.
    /// The map keys must exactly match the parameter names of the record's canonical constructor.
    ///
    /// @param <T> The record type to produce.
    /// @param map The map of field names to raw input values.
    /// @param recordClass The record class to instantiate.
    /// @return A new instance of `T` populated from the map entries.
    /// @throws MagicFactory.CreatorSelectionException If the canonical constructor cannot be selected.
    /// @throws MagicFactory.CreationException If instantiation of the record fails.
    /// @throws UnavailableConverterException If no converter is available for a field type.
    /// @throws ConvertionException If a field value cannot be converted to the required type.
    /// @throws IllegalArgumentException If `map` or `recordClass` is `null` or if `recordClass` is not a record class.
    @NonNull
    public default <T extends Record> T mapToRecord(@NonNull Map<String, ?> map, @NonNull Class<T> recordClass)
            throws MagicFactory.CreatorSelectionException,
            MagicFactory.CreationException,
            UnavailableConverterException,
            ConvertionException
    {
        if (!recordClass.isRecord()) throw new IllegalArgumentException("Not a record class.");

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
            var paramName = param.getName();
            var paramType = param.getParameterizedType();
            var value = this.get(paramType).fromObj(map.get(paramName)).orElse(null);
            args.add(value);
        }

        // Execute the constructor or method with the arguments.
        return exec.create(args.toArray());
    }

    /*@NonNull
    private static String camelCaseToSnakeCase(@NonNull String paramName) {
        checkNotNull(paramName); // Check recognized by lombok.
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