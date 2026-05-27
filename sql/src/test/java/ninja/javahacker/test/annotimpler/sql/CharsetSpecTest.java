package ninja.javahacker.test.annotimpler.sql;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public class CharsetSpecTest {

    private static final Charset CHARSET_CESU_8 = Charset.forName("CESU-8");

    private static final List<CharsetEntry> DATA = List.of(
            new CharsetEntry("ASCII"     , CharsetSpec.CS_ASCII       , CharsetSpec.Ascii            .INSTANCE),
            new CharsetEntry("UTF-8"     , CharsetSpec.CS_UTF8        , CharsetSpec.Utf8             .INSTANCE),
            new CharsetEntry("UTF-16-BE" , CharsetSpec.CS_UTF16BE     , CharsetSpec.Utf16BigEndian   .INSTANCE),
            new CharsetEntry("UTF-16-LE" , CharsetSpec.CS_UTF16LE     , CharsetSpec.Utf16LittleEndian.INSTANCE),
            new CharsetEntry("UTF-16-BOM", CharsetSpec.CS_UTF16BOM    , CharsetSpec.Utf16Bom         .INSTANCE),
            new CharsetEntry("UTF-32-BE" , CharsetSpec.CS_UTF32BE     , CharsetSpec.Utf32BigEndian   .INSTANCE),
            new CharsetEntry("UTF-32-LE" , CharsetSpec.CS_UTF32LE     , CharsetSpec.Utf32LittleEndian.INSTANCE),
            new CharsetEntry("UTF-32-BOM", CharsetSpec.CS_UTF32BOM    , CharsetSpec.Utf32Bom         .INSTANCE),
            new CharsetEntry("BIG-5"     , CharsetSpec.CS_BIG5        , CharsetSpec.Big5             .INSTANCE),
            new CharsetEntry("GBK"       , CharsetSpec.CS_GBK         , CharsetSpec.Gbk              .INSTANCE),
            new CharsetEntry("Shift_JIS" , CharsetSpec.CS_SHIFT_JIS   , CharsetSpec.ShiftJis         .INSTANCE),
            new CharsetEntry("EUC-JP"    , CharsetSpec.CS_EUC_JP      , CharsetSpec.EucJp            .INSTANCE),
            new CharsetEntry("EUC-KR"    , CharsetSpec.CS_EUC_KR      , CharsetSpec.EucKr            .INSTANCE),
            new CharsetEntry("WIN-1250"  , CharsetSpec.CS_WINDOWS_1250, CharsetSpec.Windows1250      .INSTANCE),
            new CharsetEntry("WIN-1251"  , CharsetSpec.CS_WINDOWS_1251, CharsetSpec.Windows1251      .INSTANCE),
            new CharsetEntry("WIN-1252"  , CharsetSpec.CS_WINDOWS_1252, CharsetSpec.Windows1252      .INSTANCE),
            new CharsetEntry("WIN-1254"  , CharsetSpec.CS_WINDOWS_1254, CharsetSpec.Windows1254      .INSTANCE),
            new CharsetEntry("WIN-1255"  , CharsetSpec.CS_WINDOWS_1255, CharsetSpec.Windows1255      .INSTANCE),
            new CharsetEntry("WIN-1256"  , CharsetSpec.CS_WINDOWS_1256, CharsetSpec.Windows1256      .INSTANCE),
            new CharsetEntry("WIN-1257"  , CharsetSpec.CS_WINDOWS_1257, CharsetSpec.Windows1257      .INSTANCE),
            new CharsetEntry("WIN-1258"  , CharsetSpec.CS_WINDOWS_1258, CharsetSpec.Windows1258      .INSTANCE),
            new CharsetEntry("WIN-874"   , CharsetSpec.CS_WINDOWS_874 , CharsetSpec.Windows874       .INSTANCE),
            new CharsetEntry("ISO-8859-1", CharsetSpec.CS_ISO_8859_1  , CharsetSpec.Iso88591Strict   .INSTANCE),
            new CharsetEntry("ISO-8859-2", CharsetSpec.CS_ISO_8859_2  , CharsetSpec.Iso88592         .INSTANCE),
            new CharsetEntry("ISO-8859-7", CharsetSpec.CS_ISO_8859_7  , CharsetSpec.Iso88597         .INSTANCE),
            new CharsetEntry("CESU-8"    , CHARSET_CESU_8             , Cesu8                        .INSTANCE)
    );

    public static record CharsetEntry(String name, Charset charset, CharsetSpec spec) {
    }

    public CharsetSpecTest() {
    }

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static enum Cesu8 implements CharsetSpec {
        // No @Creator, should be implicit.
        INSTANCE;

        @Override
        public Charset get() {
            return Charset.forName("Cesu8");
        }
    }

    public static enum BadCharset1 implements CharsetSpec {
        AMBIG1, AMBIG2;

        @Override
        public Charset get() {
            throw new AssertionError();
        }
    }

    public interface BadCharset2 extends CharsetSpec {
    }

    public static final class BadCharset3 implements CharsetSpec {
        public BadCharset3() {
            throw new NullPointerException("Oops");
        }

        @Override
        public Charset get() {
            throw new AssertionError();
        }
    }

    @TestFactory
    public Stream<DynamicTest> testCharsets() {
        return DATA.stream().map(d -> n("[testCharsets] " + d.name(), () -> Assertions.assertSame(d.charset(), d.spec().get())));
    }

    @TestFactory
    public Stream<DynamicTest> testFactories() {
        return DATA.stream().map(d -> n("[testFactories] " + d.name(), () -> Assertions.assertSame(d.spec(), MagicFactory.of(d.spec().getClass()).create())));
    }

    @TestFactory
    public Stream<DynamicTest> testInstance() {
        return DATA.stream().map(d -> n("[testInstance] " + d.name(), () -> Assertions.assertSame(d.spec(), CharsetSpec.instance(d.spec().getClass()))));
    }

    @TestFactory
    public Stream<DynamicTest> testBadCustomCharsets() {
        return Stream.of(BadCharset1.class, BadCharset2.class, BadCharset3.class).map(k ->
                n("[testBadCustomCharsets] " + k.getSimpleName(), () -> {
                        var ex = Assertions.assertThrows(CharsetSpec.BadCharsetSpecException.class, () -> CharsetSpec.instance(k));
                        Assertions.assertTrue(ex.getCause() instanceof MagicFactory.CreationException || ex.getCause() instanceof MagicFactory.CreatorSelectionException);
                })
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                n("[testNulls] a", () -> ForTests.testNull("cause", () -> new CharsetSpec.BadCharsetSpecException(null))),
                n("[testNulls] b", () -> ForTests.testNull("k", () -> CharsetSpec.instance(null))),
                n("[testNulls] c", () -> ForTests.testNull("input", () -> CharsetSpec.Utf8.INSTANCE.decode(null)))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testDecode() {
        var a = "Test abc éá XXX";
        var b = "Test abc 🤣💎 XXX";
        var c = "Test abc ФЯЮ XXX";
        var ca = CharsetSpec.CS_WINDOWS_1252;
        var cb = CharsetSpec.CS_UTF8;
        var cc = CharsetSpec.CS_WINDOWS_1251;
        var ba = a.getBytes(ca);
        var bb = b.getBytes(cb);
        var bc = c.getBytes(cc);
        return Stream.of(
                n("[testDecode] a", () -> Assertions.assertEquals(a, CharsetSpec.Windows1252.INSTANCE.decode(ba))),
                n("[testDecode] b", () -> Assertions.assertEquals(b, CharsetSpec.Utf8       .INSTANCE.decode(bb))),
                n("[testDecode] c", () -> Assertions.assertEquals(c, CharsetSpec.Windows1251.INSTANCE.decode(bc)))
        );
    }
}
