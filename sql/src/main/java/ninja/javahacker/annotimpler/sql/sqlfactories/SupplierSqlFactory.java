package ninja.javahacker.annotimpler.sql.sqlfactories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

/// Singleton [SqlFactory] that delegates SQL retrieval to a user-provided [SqlSupplier] class, as specified
/// by a [SqlFromClass]-annotated method.
/// The supplier class is instantiated reflectively via [ninja.javahacker.annotimpler.magicfactory.MagicFactory].
/// If the supplier's constructor accepts a single `String` argument, the value of [SqlFromClass#key()] is
/// passed to it; otherwise the no-arg constructor is used.
@SuppressFBWarnings({"ENMI_ONE_ENUM_VALUE", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY"})
public enum SupplierSqlFactory implements SqlFactory {

    /// The sole instance of this factory.
    INSTANCE;

    /// Instantiates the [SqlSupplier] class referenced by the [SqlFromClass] annotation on `m`
    /// and returns it.
    ///
    /// @param m The method carrying the [SqlFromClass] annotation.
    /// @return The instantiated [SqlSupplier].
    /// @throws BadImplementationException If the supplier class cannot be instantiated.
    /// @throws UnsupportedOperationException If `m` has no [SqlFromClass] annotation.
    /// @throws IllegalArgumentException If `m` is `null`.
    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromClass.class);
        if (anno == null) throw new UnsupportedOperationException();
        var ref = anno.value();
        try {
            var factory = MagicFactory.of(ref);
            if (factory.arity() == 0) return factory.create();
            return factory.create(anno.key());
        } catch (MagicFactory.CreationException | MagicFactory.CreatorSelectionException e) {
            throw new BadImplementationException("Can't create a SqlSupplier.", e, ref);
        }
    }
}
