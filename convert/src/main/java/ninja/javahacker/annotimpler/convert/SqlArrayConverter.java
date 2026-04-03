package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum SqlArrayConverter implements Converter<java.sql.Array> {
    INSTANCE;

    @NonNull
    @Override
    public Class<java.sql.Array> getType() {
        return java.sql.Array.class;
    }

    @NonNull
    @Override
    public Optional<java.sql.Array> from(@NonNull java.sql.Array in) {
        return Optional.of(in);
    }
}
