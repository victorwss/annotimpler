package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum RefConverter implements Converter<Ref> {
    INSTANCE;

    @NonNull
    @Override
    public Class<Ref> getType() {
        return Ref.class;
    }

    @NonNull
    @Override
    public Optional<Ref> from(@NonNull Ref in) {
        return Optional.of(in);
    }
}
