package ninja.javahacker.annotimpler.magicfactory.cvt;

import java.sql.Timestamp;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlTimestampConverter implements Converter<Timestamp> {
    INSTANCE;

    @Override
    public Timestamp from(@NonNull LocalDate in) {
        return Timestamp.valueOf(LocalDateTimeConverter.INSTANCE.from(in));
    }

    @Override
    public Timestamp from(@NonNull LocalDateTime in) {
        return Timestamp.valueOf(in);
    }

    @Override
    public Timestamp from(@NonNull OffsetDateTime in) {
        return Timestamp.valueOf(LocalDateTimeConverter.INSTANCE.from(in));
    }

    @Override
    public Timestamp from(@NonNull String in) {
        return Timestamp.valueOf(LocalDateTimeConverter.INSTANCE.from(in));
    }
}
