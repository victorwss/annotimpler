package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlArrayConverter implements Converter<java.sql.Array> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<java.sql.Array> from(@NonNull java.sql.Array in) {
        return Optional.of(in);
    }
}
