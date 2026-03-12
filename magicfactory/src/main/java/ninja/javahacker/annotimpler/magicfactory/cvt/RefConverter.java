package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum RefConverter implements Converter<Ref> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<Ref> from(@NonNull Ref in) {
        return Optional.of(in);
    }
}
