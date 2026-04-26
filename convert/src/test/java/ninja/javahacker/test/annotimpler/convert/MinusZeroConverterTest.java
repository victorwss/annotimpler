package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module org.junit.jupiter.api;

public class MinusZeroConverterTest {

    @TestFactory
    public List<DynamicNode> testMinusZero() throws Exception {
        var h = new HeavyConverterTestSupport();
        var prefix1 = "[testMinusZero - float]";
        var prefix2 = "[testMinusZero - double]";
        var prefix3 = "[testMinusZero - String]";
        var strs    = h.e(String .class, List.of("-0"));
        var floats  = h.e(float  .class, List.of(-0.0F));
        var doubles = h.e(double .class, List.of(-0.0));
        var bools   = h.e(boolean.class, List.of(false));
        var ens     = h.e(TestTypes.Color.class, List.of(TestTypes.Color.RED));

        var bigds      = strs .map(BigDecimal.class, h::bd);
        var bigis      = strs .map(BigInteger.class, h::bi);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArs   = strs .map(byte[]    .class, String::getBytes);
        var longs      = bigis.map(long      .class, h::lo);
        var bytes      = longs.map(byte      .class, h::b);
        var chars      = longs.map(char      .class, h::c);
        var shorts     = longs.map(short     .class, h::s);
        var ints       = longs.map(int       .class, h::i);
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints    .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs   .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4color    = ens     .map(TestTypes.R4Color       .class, TestTypes.R4Color    ::new);
        var r4bas      = bytesArs.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs    .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs    .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs    .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all1 = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                r4bools, r4ints, r4longs, r4doubles, r4color
        );
        var all2 = List.of(
                bools, bytes, shorts,        ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                r4bools, r4ints, r4longs, r4doubles, r4color,
                charsArs, bytesArs, r4bas
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var floatNode  = h.testIn(prefix1, cvts, floats , all1);
        var doubleNode = h.testIn(prefix2, cvts, doubles, all1);
        var strNode    = h.testIn(prefix3, cvts, strs   , all2);

        return List.of(floatNode, doubleNode, strNode);
    }
}
