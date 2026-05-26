package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface CharsetSpec {

    // All os those derived from https://html.spec.whatwg.org/multipage/parsing.html#determining-the-character-encoding
    public static final Charset CS_ASCII = StandardCharsets.US_ASCII;
    public static final Charset CS_UTF8 = StandardCharsets.UTF_8;
    public static final Charset CS_UTF16BE = StandardCharsets.UTF_16BE;
    public static final Charset CS_UTF16LE = StandardCharsets.UTF_16LE;
    public static final Charset CS_UTF16BOM = StandardCharsets.UTF_16;
    public static final Charset CS_UTF32BE = StandardCharsets.UTF_32BE;
    public static final Charset CS_UTF32LE = StandardCharsets.UTF_32LE;
    public static final Charset CS_UTF32BOM = StandardCharsets.UTF_32;
    public static final Charset CS_BIG5 = Charset.forName("Big5");
    public static final Charset CS_GBK = Charset.forName("GBK");
    public static final Charset CS_SHIFT_JIS = Charset.forName("Shift_JIS");
    public static final Charset CS_EUC_JP = Charset.forName("EUC-JP");
    public static final Charset CS_EUC_KR = Charset.forName("EUC-KR");
    public static final Charset CS_WINDOWS_1250 = Charset.forName("windows-1250");
    public static final Charset CS_WINDOWS_1251 = Charset.forName("windows-1251");
    public static final Charset CS_WINDOWS_1252 = Charset.forName("windows-1252");
    public static final Charset CS_WINDOWS_1254 = Charset.forName("windows-1254");
    public static final Charset CS_WINDOWS_1255 = Charset.forName("windows-1255");
    public static final Charset CS_WINDOWS_1256 = Charset.forName("windows-1256");
    public static final Charset CS_WINDOWS_1257 = Charset.forName("windows-1257");
    public static final Charset CS_WINDOWS_1258 = Charset.forName("windows-1258");
    public static final Charset CS_WINDOWS_874 = Charset.forName("windows-874");
    public static final Charset CS_ISO_8859_1 = StandardCharsets.ISO_8859_1;
    public static final Charset CS_ISO_8859_2 = Charset.forName("ISO-8859-2");
    public static final Charset CS_ISO_8859_7 = Charset.forName("ISO-8859-7");

    public Charset get();

    public static class BadCharsetSpecException extends IOException {

        private static final long serialVersionUID = 1L;

        public BadCharsetSpecException(@NonNull Throwable cause) {
            List.of(cause);
            super(cause);
        }
    }

    public default String decode(@NonNull byte[] input) throws IOException {
        var buf = ByteBuffer.wrap(input);
        var cs = get();
        try {
            var cb = cs.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).decode(buf);
            return new String(cb.array(), 0, cb.length());
        } catch (IOException e) {
            throw new IOException("String can't be coded as " + cs.displayName(Locale.ROOT) + ".");
        }
    }

    public static CharsetSpec instance(@NonNull Class<? extends CharsetSpec> k) throws BadCharsetSpecException {
        try {
            return MagicFactory.of(k).create();
        } catch (Throwable e) {
            throw new BadCharsetSpecException(e);
        }
    }

    public static enum Utf8 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF8;
        }
    }

    public static enum Ascii implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ASCII;
        }
    }

    public static enum Iso88591Strict implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_1;
        }
    }

    public static enum Utf16_LE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16LE;
        }
    }

    public static enum Utf16_BE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16BE;
        }
    }

    public static enum Utf16_Bom implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16BOM;
        }
    }

    public static enum Utf32_LE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32LE;
        }
    }

    public static enum Utf32_BE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32BE;
        }
    }

    public static enum Utf32_Bom implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32BOM;
        }
    }

    public static enum Big5 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_BIG5;
        }
    }

    public static enum Gbk implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_GBK;
        }
    }

    public static enum ShiftJis implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_SHIFT_JIS;
        }
    }

    public static enum EucJp implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_EUC_JP;
        }
    }

    public static enum EucKr implements CharsetSpec {
        @Creator
        INSTANCE;


        @Override
        public Charset get() {
            return CS_EUC_KR;
        }
    }

    public static enum Windows1250 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1250;
        }
    }

    public static enum Windows1251 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1251;
        }
    }

    public static enum Windows1252 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1252;
        }
    }

    public static enum Windows1254 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1254;
        }
    }

    public static enum Windows1255 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1255;
        }
    }

    public static enum Windows1256 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1256;
        }
    }

    public static enum Windows1257 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1257;
        }
    }

    public static enum Windows1258 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1258;
        }
    }

    public static enum Windows874 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_874;
        }
    }

    public static enum Iso88592 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_2;
        }
    }

    public static enum Iso88597 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_7;
        }
    }
}
