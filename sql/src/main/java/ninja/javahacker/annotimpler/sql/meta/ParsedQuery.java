package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;

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

    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '\"';
    private static final char COLON = ':';
    private static final char QMARK = '?';
    private static final String COLON_STRING = ":";

    public ParsedQuery {
        params = deepCopy2(params);
    }

    @NonNull
    private static <A, B> Map<A, List<B>> deepCopy2(@NonNull Map<A, List<B>> input) {
        if (input == null) throw new AssertionError();
        var sketch = new LinkedHashMap<A, List<B>>(input);
        sketch.replaceAll((k, v) -> List.copyOf(v));
        return Map.copyOf(sketch);
    }

    @NonNull
    private static Optional<String> readName(@NonNull String original, int i) {
        if (original == null) throw new AssertionError();
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

    @NonNull
    public static ParsedQuery parse(@NonNull String original) {
        var lacunas = new HashMap<String, List<Integer>>(10);
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
                        c = QMARK; // Substitui o parâmetro por um ponto de interrogação.
                        i += name.length(); // Pula o resto do parâmetro.
                        var p = index;
                        lacunas.compute(name, (k, v) -> {
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
                lacunas,
                index - 1,
                unnamedParameters > 0,
                inSingleQuote || inDoubleQuote,
                loneColonsSoFar > 0
        );
    }

    public boolean hasErrors() {
        return unclosedQuotes() || unnamedParameters() || loneColons();
    }
}
