package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlArrayConverter implements Converter<java.sql.Array> {
    INSTANCE;

    @Override
    public java.sql.Array from(@NonNull java.sql.Array in) {
        return in;
    }
}
