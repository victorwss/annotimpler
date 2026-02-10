package ninja.javahacker.annotimpler.magicfactory.cvt;

import java.util.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum DateConverter implements Converter<Date> {
    INSTANCE;

    @Override
    public Date from(@NonNull LocalDate in) {
        return Date.from(InstantConverter.INSTANCE.from(in));
    }

    @Override
    public Date from(@NonNull LocalDateTime in) {
        return Date.from(InstantConverter.INSTANCE.from(in));
    }

    @Override
    public Date from(@NonNull OffsetDateTime in) {
        return Date.from(InstantConverter.INSTANCE.from(in));
    }

    @Override
    public Date from(@NonNull String in) {
        return Date.from(InstantConverter.INSTANCE.from(in));
    }
}
