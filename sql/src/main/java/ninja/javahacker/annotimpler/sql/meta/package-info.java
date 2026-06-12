/// Meta-infrastructure for the SQL implementation layer.
///
/// This package contains the building blocks used internally by the SQL implementation
/// classes to load SQL strings, handle named parameters, and resolve the appropriate
/// [SqlFactory] for each annotated method.
///
/// Key types include:
/// - [SqlSupplier] and [ParsedSqlSupplier] — functional interfaces for obtaining raw and
///   parsed SQL strings at call time.
/// - [ParsedQuery] — immutable value type holding a parsed SQL string with its parameter map.
/// - [SqlFactory] and [SqlSource] — the meta-annotation mechanism that connects
///   a method-level SQL annotation to the factory class responsible for producing the SQL.
/// - [ParameterSet] and [ParameterReceiver] — the parameter-binding strategy abstraction
///   that maps method arguments to named SQL parameters.
/// - [ThreadTracerSqlSupplier] — a thread-safe, lazily initialised [SqlSupplier] wrapper.
/// - [ConnectionFactoryKeyProperty], [ConverterFactoryKeyProperty], and
///   [LocalizerKeyProperty] — typed [ninja.javahacker.annotimpler.core.KeyProperty] singletons
///   used to store configuration values in a [ninja.javahacker.annotimpler.core.PropertyBag].
package ninja.javahacker.annotimpler.sql.meta;