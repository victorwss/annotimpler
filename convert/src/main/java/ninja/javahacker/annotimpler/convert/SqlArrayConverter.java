package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum SqlArrayConverter implements Converter<java.sql.Array> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<java.sql.Array> from(@NonNull java.sql.Array in) {
        return Optional.of(in);
    }
}
