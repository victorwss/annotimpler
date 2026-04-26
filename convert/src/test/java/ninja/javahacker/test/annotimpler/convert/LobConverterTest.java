package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class LobConverterTest {

    private Blob blob(String in) {
        return (Blob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Blob.class }, (i, m, a) -> {
            if (m.getName().equals("getBinaryStream")) return new ByteArrayInputStream(in.getBytes());
            throw new AssertionError(m.getName());
        });
    }

    private NClob nclob(String in) {
        return (NClob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { NClob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return new StringReader(in);
            throw new AssertionError(m.getName());
        });
    }

    private Clob clob(String in) {
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return new StringReader(in);
            throw new AssertionError(m.getName());
        });
    }

    private SQLXML sqlxml(String in) {
        return (SQLXML) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { SQLXML.class }, (i, m, a) -> {
            if (m.getName().equals("getString")) return in;
            throw new AssertionError(m.getName());
        });
    }

    @TestFactory
    public List<DynamicNode> testLobTypes() throws Exception {
        var h = new HeavyConverterTestSupport();
        var prefix1 = "[testLobTypes - String]";
        var prefix2 = "[testLobTypes - byte[]]";
        var prefix3 = "[testLobTypes - Blob]";
        var prefix4 = "[testLobTypes - Clob]";
        var prefix5 = "[testLobTypes - NClob]";
        var prefix6 = "[testLobTypes - SQLXML]";
        var strs    = h.e(String.class, List.of("bla bla bla", "lorem ipsum dolor sit amet"));
        var bytes   = strs.map(byte[].class, String::getBytes);
        var chars   = strs.map(char[].class, String::toCharArray);
        var blobs   = strs.map(Blob  .class, this::blob);
        var clobs   = strs.map(Clob  .class, this::clob);
        var nclobs  = strs.map(NClob .class, this::nclob);
        var xmls    = strs.map(SQLXML.class, this::sqlxml);
        var r4bas   = bytes  .map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA = strs   .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB = strs   .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC = r4strsB.map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD = strs   .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE = r4strsC.map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF = r4strsE.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all = List.of(strs, bytes, blobs, clobs, nclobs, xmls, chars, r4bas, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF);

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var strNode   = h.testIn(prefix1, cvts, strs  , all);
        var bytsNode  = h.testIn(prefix2, cvts, bytes , all);
        var blobNode  = h.testIn(prefix3, cvts, blobs , all);
        var clobNode  = h.testIn(prefix4, cvts, clobs , all);
        var nclobNode = h.testIn(prefix5, cvts, nclobs, all);
        var xmlNode   = h.testIn(prefix6, cvts, xmls  , all);

        return List.of(strNode, bytsNode, blobNode, clobNode, nclobNode, xmlNode);
    }
}
