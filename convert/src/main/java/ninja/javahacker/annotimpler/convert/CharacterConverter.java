package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum CharacterConverter implements Converter<Character> {
    PRIMITIVE, WRAPPER;

    @NonNull
    @Override
    public Class<Character> getType() {
        return this == PRIMITIVE ? char.class : Character.class;
    }

    @NonNull
    @Override
    public Optional<Character> fromNull() {
        return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Character> from(byte in) {
        return Optional.of((char) in);
    }

    @NonNull
    @Override
    public Optional<Character> from(short in) {
        return Optional.of((char) in);
    }

    @NonNull
    @Override
    public Optional<Character> from(int in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(long in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(float in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(float.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(double in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return from(in.intValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull String in) throws ConvertionException {
        if (in.length() == 0) return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
        if (in.length() != 1) throw new ConvertionException(String.class, getType());
        return Optional.of(in.charAt(0));
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull byte[] in) throws ConvertionException {
        if (in.length == 0) return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
        if (in.length != 1) throw new ConvertionException(byte[].class, getType());
        return from(in[0]);
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull Blob in) throws ConvertionException {
        try {
            return from(new String(in.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(Blob.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull Clob in) throws ConvertionException {
        try {
            return from(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(Clob.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull NClob in) throws ConvertionException {
        try {
            return from(in.getCharacterStream().readAllAsString());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(NClob.class, getType());
        }
    }
}
