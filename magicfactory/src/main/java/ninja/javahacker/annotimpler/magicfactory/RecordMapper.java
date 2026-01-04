package ninja.javahacker.annotimpler.magicfactory;

import module java.base;

import lombok.NonNull;

public final class RecordMapper {

    public RecordMapper() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static <T> T forMap(@NonNull Map<String, ?> map, @NonNull Class<T> someClass) throws ConstructionException {
        if (someClass.isRecord()) return someClass.cast(mapToRecord(map, someClass.asSubclass(Record.class)));
        if (map.size() != 1) throw new IllegalArgumentException("Failed to create instance.");
        return MagicConverter.forValue(map.values().toArray()[0], someClass);
    }

    @NonNull
    private static <T extends Record> T mapToRecord(@NonNull Map<String, ?> map, @NonNull Class<T> recordClass)
            throws ConstructionException
    {
        // Seleciona o construtor ou método mais adequado.
        var exec = MagicFactory.of(recordClass);

        // Prepara os argumentos para o construtor ou método.
        var args = prepareArguments(exec, map);

        return exec.create(args.toArray());
    }

    @NonNull
    private static List<?> prepareArguments(
            @NonNull MagicFactory<?> constructor,
            @NonNull Map<String, ?> map)
            throws ConstructionException
    {
        var parameters = constructor.getParameters();
        var args = new ArrayList<Object>(parameters.size());

        for (var param : parameters) {
            var paramName = camelCaseToSnakeCase(param.getName());
            var paramType = param.getParameterizedType();
            args.add(MagicConverter.forValue(map.get(paramName), paramType));
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