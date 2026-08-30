/// Stream wrappers that enforce a maximum byte/character read limit.
///
/// This module provides [ninja.javahacker.limited.LimitedInputStream] and
/// [ninja.javahacker.limited.LimitedReader], decorators around a JDK `InputStream`
/// or `Reader` that cap the number of bytes/characters that can be read from the wrapped
/// stream, while still supporting `mark`/`reset`. They are typically used to defensively bound
/// how much data is consumed from a `Blob`/`Clob` or similar externally-controlled source.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.limited {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    exports ninja.javahacker.limited;
}
