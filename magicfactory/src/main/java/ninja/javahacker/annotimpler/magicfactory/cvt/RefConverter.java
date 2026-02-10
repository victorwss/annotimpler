package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.magicfactory;

public enum RefConverter implements Converter<Ref> {
    INSTANCE;

    @Override
    public Ref from(@NonNull Ref in) {
        return in;
    }
}
