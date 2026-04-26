package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module org.junit.jupiter.api;

public class NumericConverterTest {

    @TestFactory
    public List<DynamicNode> testNumericTypes() throws Exception {
        var h = new HeavyConverterTestSupport();
        var prefix1 = "[testNumericTypes - byte]";
        var prefix2 = "[testNumericTypes - short]";
        var prefix3 = "[testNumericTypes - int]";
        var prefix4 = "[testNumericTypes - long]";
        var prefix5 = "[testNumericTypes - float]";
        var prefix6 = "[testNumericTypes - double]";
        var prefix7 = "[testNumericTypes - BigDecimal]";
        var prefix8 = "[testNumericTypes - String]";
        var strs = h.e(String.class, List.of(
                "q", "xxx", "RED",
                "0", "1", "9", "42", "55", "127", "-30", "-128", "32000", "64000",
                "489876544", "12345678910", "9876543210987654", "98765432109876543210",
                "16777217", "9007199254740993",
                "3.5", "0.078", "-177.77", "98765432109876543210.98765432",
                "NaN", "Infinity", "-Infinity"
        ));
        var floats = h.e(float.class, Arrays.asList(
                null, null, null,
                0F, 1F, 9F, 42F, 55F, 127F, -30F, -128F, 32000F, 64000F,
                489876544F, null, null, null,
                null, null,
                3.5F, 0.078F, -177.77F, null,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
        ));
        var doubles = h.e(double.class, Arrays.asList(
                null, null, null,
                0D, 1D, 9D, 42D, 55D, 127D, -30D, -128D, 32000D, 64000D,
                489876544D, 12345678910D, 9876543210987654D, null,
                16777217.0D, null,
                3.5D, 0.078D, -177.77D, null,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        ));
        var chs   = h.e(char           .class, Arrays.asList('q', null, null, '0', '1', '9'));
        var ens   = h.e(TestTypes.Color.class, Arrays.asList(null, null, TestTypes.Color.RED, TestTypes.Color.RED, TestTypes.Color.GREEN));
        var bools = h.e(boolean        .class, Arrays.asList(null, null, null, false, true));

        var bigds      = strs .map(BigDecimal.class, h::bd);
        var bigis      = strs .map(BigInteger.class, h::bi);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArs   = strs .map(byte[]    .class, String::getBytes);
        var longs      = bigis.map(long      .class, h::lo);
        var bytes      = longs.map(byte      .class, h::b );
        var chars      = longs.map(char      .class, h::c );
        var shorts     = longs.map(short     .class, h::s );
        var ints       = longs.map(int       .class, h::i );
        var bytesA     = bytes.map(byte[]    .class, x -> new byte[] {x});
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints    .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs   .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4color    = ens     .map(TestTypes.R4Color       .class, TestTypes.R4Color    ::new);
        var r4bas1     = bytesArs.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4bas2     = bytesA  .map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs    .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs    .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs    .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all  = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF
        );
        var allB = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesA, r4bas2
        );
        var allC = List.of(
                bools, bytes, shorts, chs  , ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                charsArs, bytesArs, r4bas1
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var byteNode   = h.testIn(prefix1, cvts, bytes  , allB);
        var shortNode  = h.testIn(prefix2, cvts, shorts , all );
        var intNode    = h.testIn(prefix3, cvts, ints   , all );
        var longNode   = h.testIn(prefix4, cvts, longs  , all );
        var floatNode  = h.testIn(prefix5, cvts, floats , all );
        var doubleNode = h.testIn(prefix6, cvts, doubles, all );
        var bigdNode   = h.testIn(prefix7, cvts, bigds  , all );
        var strNode    = h.testIn(prefix8, cvts, strs   , allC);

        return List.of(byteNode, shortNode, intNode, longNode, floatNode, doubleNode, bigdNode, strNode);
    }
}
