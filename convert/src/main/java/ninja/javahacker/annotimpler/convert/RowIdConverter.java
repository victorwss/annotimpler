package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum RowIdConverter implements Converter<RowId> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<RowId> from(@NonNull RowId in) {
        return Optional.of(in);
    }
}
