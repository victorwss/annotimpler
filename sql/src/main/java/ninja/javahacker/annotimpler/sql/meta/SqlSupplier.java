package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlSupplier {
    public String get() throws SQLException;
}
