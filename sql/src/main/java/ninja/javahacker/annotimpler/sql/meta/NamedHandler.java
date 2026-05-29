package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.sql;
import module ninja.javahacker.annotimpler.sql;

@PackagePrivate
@FunctionalInterface
interface NamedHandler<T> {
    public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException;
}