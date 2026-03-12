package ninja.javahacker.annotimpler.magicfactory;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class RecordMapper {

    @NonNull
    private final ConverterFactory factory;

    public RecordMapper(@NonNull ConverterFactory factory) {
        this.factory = factory;
    }

    /*@NonNull
    public <T> Optional<T> forMap(@NonNull Map<String, ?> map, @NonNull Class<T> someClass)
            throws MagicFactory.CreatorSelectionException,
            MagicFactory.CreationException,
            ConverterFactory.UnavailableConverterException,
            Converter.ConvertionException
    {
        if (someClass.isRecord()) return Optional.of(someClass.cast(mapToRecord(map, someClass.asSubclass(Record.class))));
        if (map.size() != 1) throw new IllegalArgumentException("Failed to create instance.");
        var value = map.values().iterator().next();
        return factory.get(someClass).from(value);
    }*/

    @NonNull
    public <T extends Record> T mapToRecord(@NonNull Map<String, ?> map, @NonNull Class<T> recordClass)
            throws MagicFactory.CreatorSelectionException,
            MagicFactory.CreationException,
            ConverterFactory.UnavailableConverterException,
            Converter.ConvertionException
    {
        // Seleciona o construtor ou método mais adequado.
        var exec = MagicFactory.of(recordClass);

        // Prepara os argumentos para o construtor ou método.
        var args = prepareArguments(exec, map);

        return exec.create(args.toArray());
    }

    @NonNull
    private List<?> prepareArguments(
            @NonNull MagicFactory<?> constructor,
            @NonNull Map<String, ?> map)
            throws ConverterFactory.UnavailableConverterException, Converter.ConvertionException
    {
        var parameters = constructor.getParameters();
        var args = new ArrayList<Object>(parameters.size());

        for (var param : parameters) {
            var paramName = camelCaseToSnakeCase(param.getName());
            var paramType = param.getParameterizedType();
            var value = factory.get(paramType).from(map.get(paramName)).orElse(null);
            args.add(value);
        }

        return args;
    }

    @NonNull
    private static String camelCaseToSnakeCase(@NonNull String paramName) {
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
}