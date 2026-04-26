package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module org.junit.jupiter.api;

public class BooleanConverterTest {

    @TestFactory
    public List<DynamicNode> testBooleanTypes() throws Exception {
        var h = new HeavyConverterTestSupport();
        var prefix1 = "[testBooleanTypes - from boolean]";
        var prefix2 = "[testBooleanTypes - from String]";
        var longs     = h.e(long   .class, List.of(0L, 1L));
        var floats    = h.e(float  .class, List.of(0F, 1F));
        var doubles   = h.e(double .class, List.of(0D, 1D));
        var bools1    = h.e(boolean.class, List.of(false, true));
        var bools2    = h.e(boolean.class, List.of(false, true, false, true));
        var strs      = h.e(String .class, List.of("false", "true", "FALSE", "TRUE"));
        var bytesArs1 = h.e(byte[] .class, List.of(new byte[] {0}, new byte[] {1}));
        var bigds      = longs.map(BigDecimal.class, BigDecimal::valueOf);
        var bigis      = longs.map(BigInteger.class, BigInteger::valueOf);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArsS  = strs .map(byte[]    .class, String::getBytes);
        var bytes      = longs.map(byte      .class, h::b);
        var chars      = longs.map(char      .class, h::c);
        var shorts     = longs.map(short     .class, h::s);
        var ints       = longs.map(int       .class, h::i);
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools2   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints     .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs    .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles  .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4bas1     = bytesArs1.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4basS     = bytesArsS.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs     .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs     .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB  .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs     .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC  .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE  .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var allB = List.of(
                bools1, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesArs1, r4bas1, r4bools, r4ints, r4longs, r4doubles
        );
        var allD = List.of(
                bools2,                charsArs,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesArsS, r4basS, r4bools
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var boolNode = h.testIn(prefix1, cvts, bools1, allB);
        var str2Node = h.testIn(prefix2, cvts, strs  , allD);

        return List.of(boolNode, str2Node);
    }
}
