package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.magicfactory;

public enum RowIdConverter implements Converter<RowId> {
    INSTANCE;

    @Override
    public RowId from(@NonNull RowId in) {
        return in;
    }
}
