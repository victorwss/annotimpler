package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum RowIdConverter implements Converter<RowId> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<RowId> from(@NonNull RowId in) {
        return Optional.of(in);
    }
}
