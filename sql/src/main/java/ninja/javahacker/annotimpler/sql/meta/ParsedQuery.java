package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// An immutable value type that holds a SQL string after its named parameters have been parsed.
///
/// Use [parse] to create an instance from a raw SQL string.  The resulting object holds both
/// the original and the transformed SQL (with named parameters replaced by `?`), along with a
/// map from each parameter name to the list of 1-based positional indices at which it appears.
///
/// Error conditions (unclosed quotes, unnamed `?` parameters, or lone colons) are exposed as
/// boolean components and can be tested collectively with [hasErrors].
///
/// @param original The original SQL string as provided by the caller, before any parameter
///                 substitution; must not be `null`.
/// @param parsed The SQL string with all named parameters (`:name`) replaced by `?` placeholders,
///               ready to be passed to a `PreparedStatement`; must not be `null`.
/// @param params An unmodifiable map from each parameter name to the list of 1-based positional
///               indices at which that parameter appears in the parsed SQL; must not be `null`.
/// @param size The total number of `?` placeholders in the parsed SQL (named and unnamed combined).
/// @param unnamedParameters `true` if the original SQL contains at least one unnamed `?` parameter.
/// @param unclosedQuotes `true` if the original SQL has an unclosed single-quote or double-quote
///                       string literal.
/// @param loneColons `true` if the original SQL contains at least one bare `:` that is not
///                   followed by a valid Java identifier start character.
///
/// @see #parse(String)
/// @see #hasErrors()
@SuppressFBWarnings("EI_EXPOSE_REP")
public record ParsedQuery(
        @NonNull String original,
        @NonNull String parsed,
        @NonNull Map<String, List<Integer>> params,
        int size,
        boolean unnamedParameters,
        boolean unclosedQuotes,
        boolean loneColons)
{

    /// Character marking the start/end of a single-quoted string literal.
    private static final char SINGLE_QUOTE = '\'';

    /// Character marking the start/end of a double-quoted string literal.
    private static final char DOUBLE_QUOTE = '\"';

    /// Character introducing a named parameter (`:name`).
    private static final char COLON = ':';

    /// Character representing an unnamed positional parameter placeholder.
    private static final char QMARK = '?';

    /// String form of [#COLON], used for `equals` comparisons against parsed names.
    private static final String COLON_STRING = ":";

    /// Creates a [ParsedQuery], defensively copying `params` to make it unmodifiable.
    ///
    /// Each value list in `params` is also copied so that external changes to the original
    /// collections cannot affect this record.
    ///
    /// @throws IllegalArgumentException If `original`, `parsed`, or `params` is `null`.
    public ParsedQuery {
        params = deepCopy2(params);
    }

    @NonNull
    private static <A, B> Map<A, List<B>> deepCopy2(@NonNull Map<A, List<B>> input) {
        checkNotNull(input); // Check recognized by lombok.
        var sketch = new LinkedHashMap<A, List<B>>(input);
        sketch.replaceAll((k, v) -> List.copyOf(v));
        return Map.copyOf(sketch);
    }

    @NonNull
    private static Optional<String> readName(@NonNull String original, int i) {
        checkNotNull(original); // Check recognized by lombok.
        var c = original.charAt(i);
        if (c != COLON) return Optional.empty();
        var length = original.length();
        if (i + 1 >= length) return Optional.of(COLON_STRING);
        var nom = Character.isJavaIdentifierStart(original.charAt(i + 1));
        if (!nom) return Optional.of(COLON_STRING);
        var j = i + 2;
        while (true) {
            while (j < length && Character.isJavaIdentifierPart(original.charAt(j))) {
                j++;
            }
            if (j + 2 >= length
                    || original.charAt(j) != COLON
                    || original.charAt(j + 1) != COLON
                    || !Character.isJavaIdentifierStart(original.charAt(j + 2)))
            {
                break;
            }
            j += 3;
        }
        return Optional.of(original.substring(i + 1, j));
    }

    /// Parses a SQL string with named parameters (`:name` syntax) into a [ParsedQuery].
    ///
    /// Each named parameter of the form `:identifier` (where `identifier` is a valid Java
    /// identifier, optionally qualified with `::` for type-qualified names) is replaced by a
    /// `?` placeholder in the returned [ParsedQuery#parsed] string.  The positional index (1-based)
    /// at which each named parameter appears is recorded in [ParsedQuery#params].
    ///
    /// Bare `?` characters outside of quoted strings are counted as unnamed parameters and set
    /// [ParsedQuery#unnamedParameters] to `true`.  Unclosed single- or double-quote literals set
    /// [ParsedQuery#unclosedQuotes] to `true`.  A lone `:` not followed by a valid identifier
    /// start character sets [ParsedQuery#loneColons] to `true`.
    ///
    /// @param original The raw SQL string to parse; must not be `null`.
    /// @return A [ParsedQuery] representing the parsed result; never `null`.
    /// @throws IllegalArgumentException If `original` is `null`.
    @NonNull
    @SuppressWarnings("AssignmentToForLoopParameter")
    public static ParsedQuery parse(@NonNull String original) {
        var params = new HashMap<String, List<Integer>>(10);
        var unnamedParameters = 0;
        var length = original.length();
        var parsedQuery = new StringBuilder(length);
        var inSingleQuote = false;
        var inDoubleQuote = false;
        var loneColonsSoFar = 0;
        var index = 1;

        for (var i = 0; i < length; i++) {
            var c = original.charAt(i);
            if (inSingleQuote) {
                if (c == SINGLE_QUOTE) inSingleQuote = false;
            } else if (inDoubleQuote) {
                if (c == DOUBLE_QUOTE) inDoubleQuote = false;
            } else if (c == SINGLE_QUOTE) {
                inSingleQuote = true;
            } else if (c == DOUBLE_QUOTE) {
                inDoubleQuote = true;
            } else if (c == QMARK) {
                unnamedParameters++;
                index++;
            } else {
                var opt = readName(original, i);
                if (!opt.isEmpty()) {
                    var name = opt.get();
                    if (COLON_STRING.equals(name)) {
                        loneColonsSoFar++;
                    } else {
                        c = QMARK; // Replaces the parameter with a question mark.
                        i += name.length(); // Skip the rest of the parameter.
                        var p = index;
                        params.compute(name, (k, v) -> {
                            var v2 = v == null ? new ArrayList<Integer>(10) : v;
                            v2.add(p);
                            return v2;
                        });
                        index++;
                    }
                }
            }
            parsedQuery.append(c);
        }

        return new ParsedQuery(
                original,
                parsedQuery.toString(),
                params,
                index - 1,
                unnamedParameters > 0,
                inSingleQuote || inDoubleQuote,
                loneColonsSoFar > 0
        );
    }

    /// Returns `true` if this query has any detected error condition.
    ///
    /// A query has errors when at least one of [unclosedQuotes], [unnamedParameters],
    /// or [loneColons] is `true`.
    ///
    /// @return `true` if any error flag is set; `false` if the query appears well-formed.
    public boolean hasErrors() {
        return unclosedQuotes() || unnamedParameters() || loneColons();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
