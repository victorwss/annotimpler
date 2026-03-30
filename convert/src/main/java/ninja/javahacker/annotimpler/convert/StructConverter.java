package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum StructConverter implements Converter<Struct> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<Struct> from(@NonNull Struct in) {
        return Optional.of(in);
    }
}
