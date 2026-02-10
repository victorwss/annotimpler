package ninja.javahacker.annotimpler.magicfactory.cvt;

import java.sql.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlDateConverter implements Converter<Date> {
    INSTANCE;

    @Override
    public Date from(@NonNull LocalDate in) {
        return Date.valueOf(in);
    }

    @Override
    public Date from(@NonNull LocalDateTime in) {
        return Date.valueOf(LocalDateConverter.INSTANCE.from(in));
    }

    @Override
    public Date from(@NonNull OffsetDateTime in) {
        return Date.valueOf(LocalDateConverter.INSTANCE.from(in));
    }

    @Override
    public Date from(@NonNull String in) {
        return Date.valueOf(LocalDateConverter.INSTANCE.from(in));
    }
}
