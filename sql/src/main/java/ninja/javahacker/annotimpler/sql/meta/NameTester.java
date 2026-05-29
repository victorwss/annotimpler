package ninja.javahacker.annotimpler.sql.meta;

import java.util.Set;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

@PackagePrivate
@FunctionalInterface
interface NameTester {
    public boolean test(@NonNull Set<String> names);
}