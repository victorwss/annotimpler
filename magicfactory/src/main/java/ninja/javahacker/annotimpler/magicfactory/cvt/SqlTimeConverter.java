package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlTimeConverter implements Converter<Time> {
    INSTANCE;

    @Override
    public Time from(@NonNull LocalTime in) {
        return Time.valueOf(in);
    }

    @Override
    public Time from(@NonNull LocalDateTime in) {
        return Time.valueOf(LocalTimeConverter.INSTANCE.from(in));
    }

    @Override
    public Time from(@NonNull OffsetTime in) {
        return Time.valueOf(LocalTimeConverter.INSTANCE.from(in));
    }

    @Override
    public Time from(@NonNull OffsetDateTime in) {
        return Time.valueOf(LocalTimeConverter.INSTANCE.from(in));
    }

    @Override
    public Time from(@NonNull String in) {
        return Time.valueOf(LocalTimeConverter.INSTANCE.from(in));
    }
}
