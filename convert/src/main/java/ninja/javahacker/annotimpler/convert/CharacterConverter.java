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
    public Optional<Character> from(boolean in) {
        return Optional.of(in ? (char) 1 : (char) 0);
    }

    @NonNull
    @Override
    public Optional<Character> from(byte in) throws ConvertionException {
        var a = (char) (0x7F & in);
        if (in != (byte) a) throw new ConvertionException(byte.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(short in) throws ConvertionException {
        var a = (char) (0x7FFF & in);
        if (in != (short) a) throw new ConvertionException(short.class, getType());
        return Optional.of(a);
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
            var i = in.intValueExact();
            var a = (char) i;
            if (i != a) throw new ConvertionException(BigDecimal.class, getType());
            return Optional.of(a);
        } catch (ArithmeticException x) {
            throw new ConvertionException(BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull String in) throws ConvertionException {
        if (in.length() == 0) return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
        Character c = in.charAt(0);
        if (!in.equals(c.toString())) throw new ConvertionException(String.class, getType());
        return Optional.of(c);
    }
}
