package ninja.javahacker.annotimpler.sql;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

/// A provider of a [Charset] used to decode raw bytes into a SQL string when loading
/// SQL from a file, classpath resource, or URL.
///
/// Implementations are typically singleton enums, but are also obtainable via
/// [ninja.javahacker.annotimpler.magicfactory.MagicFactory]. A set of standard
/// implementations covering the most common encodings is provided as nested enum types.
///
/// The constants prefixed with `CS_` expose the corresponding [Charset] instances
/// for direct use in calling code.
@FunctionalInterface
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public interface CharsetSpec {

    /// US-ASCII charset constant.
    public static final Charset CS_ASCII = StandardCharsets.US_ASCII;

    /// UTF-8 charset constant.
    public static final Charset CS_UTF8 = StandardCharsets.UTF_8;

    /// UTF-16 big-endian (without BOM) charset constant.
    public static final Charset CS_UTF16BE = StandardCharsets.UTF_16BE;

    /// UTF-16 little-endian (without BOM) charset constant.
    public static final Charset CS_UTF16LE = StandardCharsets.UTF_16LE;

    /// UTF-16 with BOM charset constant.
    public static final Charset CS_UTF16BOM = StandardCharsets.UTF_16;

    /// UTF-32 big-endian (without BOM) charset constant.
    public static final Charset CS_UTF32BE = StandardCharsets.UTF_32BE;

    /// UTF-32 little-endian (without BOM) charset constant.
    public static final Charset CS_UTF32LE = StandardCharsets.UTF_32LE;

    /// UTF-32 with BOM charset constant.
    public static final Charset CS_UTF32BOM = StandardCharsets.UTF_32;

    /// Traditional Chinese Big5 charset constant.
    public static final Charset CS_BIG5 = Charset.forName("Big5");

    /// Simplified Chinese GBK charset constant.
    public static final Charset CS_GBK = Charset.forName("GBK");

    /// Japanese Shift_JIS charset constant.
    public static final Charset CS_SHIFT_JIS = Charset.forName("Shift_JIS");

    /// Japanese EUC-JP charset constant.
    public static final Charset CS_EUC_JP = Charset.forName("EUC-JP");

    /// Korean EUC-KR charset constant.
    public static final Charset CS_EUC_KR = Charset.forName("EUC-KR");

    /// Central European Windows-1250 charset constant.
    public static final Charset CS_WINDOWS_1250 = Charset.forName("windows-1250");

    /// Cyrillic Windows-1251 charset constant.
    public static final Charset CS_WINDOWS_1251 = Charset.forName("windows-1251");

    /// Western European Windows-1252 charset constant.
    public static final Charset CS_WINDOWS_1252 = Charset.forName("windows-1252");

    /// Turkish Windows-1254 charset constant.
    public static final Charset CS_WINDOWS_1254 = Charset.forName("windows-1254");

    /// Hebrew Windows-1255 charset constant.
    public static final Charset CS_WINDOWS_1255 = Charset.forName("windows-1255");

    /// Arabic Windows-1256 charset constant.
    public static final Charset CS_WINDOWS_1256 = Charset.forName("windows-1256");

    /// Baltic Windows-1257 charset constant.
    public static final Charset CS_WINDOWS_1257 = Charset.forName("windows-1257");

    /// Vietnamese Windows-1258 charset constant.
    public static final Charset CS_WINDOWS_1258 = Charset.forName("windows-1258");

    /// Thai Windows-874 charset constant.
    public static final Charset CS_WINDOWS_874 = Charset.forName("windows-874");

    /// ISO 8859-1 (Latin-1) charset constant.
    public static final Charset CS_ISO_8859_1 = StandardCharsets.ISO_8859_1;

    /// ISO 8859-2 (Latin-2, Central European) charset constant.
    public static final Charset CS_ISO_8859_2 = Charset.forName("ISO-8859-2");

    /// ISO 8859-7 (Greek) charset constant.
    public static final Charset CS_ISO_8859_7 = Charset.forName("ISO-8859-7");

    /// Returns the [Charset] represented by this spec.
    ///
    /// @return The non-null charset.
    public Charset get();

    /// Thrown when a [CharsetSpec] implementation cannot be instantiated.
    public static class BadCharsetSpecException extends IOException {

        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a new `BadCharsetSpecException` with the given cause.
        ///
        /// @param cause The underlying throwable that prevented instantiation.
        /// @throws IllegalArgumentException If `cause` is `null`.
        public BadCharsetSpecException(@NonNull Throwable cause) {
            List.of(cause);
            super(cause);
        }

        /// Disabled. Should not be used. Does nothing.
        ///
        /// This method exists with the sole purpose of fixing SpotBugs' CT_CONSTRUCTOR_THROW
        /// by disabling the ability to override the `finalize()` method that should not even exist to start with.
        ///
        /// @deprecated Finalization was deprecated. This method is intentionally unused, unusable and disabled.
        @Deprecated
        @SuppressWarnings({"override", "removal", "FinalizeDoesntCallSuperFinalize", "FinalizeDeclaration"})
        protected final void finalize() {
        }
    }

    /// Decodes a byte array into a string using the charset returned by [#get()].
    ///
    /// @param input The byte array to decode.
    /// @return The decoded string.
    /// @throws IOException If the bytes cannot be decoded using this charset.
    /// @throws IllegalArgumentException If `input` is `null`.
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

    /// Instantiates a `CharsetSpec` of the given implementation class using
    /// [ninja.javahacker.annotimpler.magicfactory.MagicFactory].
    ///
    /// @param k The `CharsetSpec` implementation class to instantiate.
    /// @return A non-null instance of `k`.
    /// @throws BadCharsetSpecException If the class cannot be instantiated.
    /// @throws IllegalArgumentException If `k` is `null`.
    public static CharsetSpec instance(@NonNull Class<? extends CharsetSpec> k) throws BadCharsetSpecException {
        try {
            return MagicFactory.of(k).create();
        } catch (Throwable e) {
            throw new BadCharsetSpecException(e);
        }
    }

    /// [CharsetSpec] implementation for UTF-8.
    public static enum Utf8 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF8;
        }
    }

    /// [CharsetSpec] implementation for US-ASCII.
    public static enum Ascii implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ASCII;
        }
    }

    /// [CharsetSpec] implementation for ISO 8859-1 (Latin-1), with strict unmappable-character reporting.
    public static enum Iso88591Strict implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_1;
        }
    }

    /// [CharsetSpec] implementation for UTF-16 little-endian (without BOM).
    public static enum Utf16LittleEndian implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16LE;
        }
    }

    /// [CharsetSpec] implementation for UTF-16 big-endian (without BOM).
    public static enum Utf16BigEndian implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16BE;
        }
    }

    /// [CharsetSpec] implementation for UTF-16 with BOM.
    public static enum Utf16Bom implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF16BOM;
        }
    }

    /// [CharsetSpec] implementation for UTF-32 little-endian (without BOM).
    public static enum Utf32LittleEndian implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32LE;
        }
    }

    /// [CharsetSpec] implementation for UTF-32 big-endian (without BOM).
    public static enum Utf32BigEndian implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32BE;
        }
    }

    /// [CharsetSpec] implementation for UTF-32 with BOM.
    public static enum Utf32Bom implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_UTF32BOM;
        }
    }

    /// [CharsetSpec] implementation for Big5 (Traditional Chinese).
    public static enum Big5 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_BIG5;
        }
    }

    /// [CharsetSpec] implementation for GBK (Simplified Chinese).
    public static enum Gbk implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_GBK;
        }
    }

    /// [CharsetSpec] implementation for Shift_JIS (Japanese).
    public static enum ShiftJis implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_SHIFT_JIS;
        }
    }

    /// [CharsetSpec] implementation for EUC-JP (Japanese).
    public static enum EucJp implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_EUC_JP;
        }
    }

    /// [CharsetSpec] implementation for EUC-KR (Korean).
    public static enum EucKr implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_EUC_KR;
        }
    }

    /// [CharsetSpec] implementation for Windows-1250 (Central European).
    public static enum Windows1250 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1250;
        }
    }

    /// [CharsetSpec] implementation for Windows-1251 (Cyrillic).
    public static enum Windows1251 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1251;
        }
    }

    /// [CharsetSpec] implementation for Windows-1252 (Western European).
    public static enum Windows1252 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1252;
        }
    }

    /// [CharsetSpec] implementation for Windows-1254 (Turkish).
    public static enum Windows1254 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1254;
        }
    }

    /// [CharsetSpec] implementation for Windows-1255 (Hebrew).
    public static enum Windows1255 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1255;
        }
    }

    /// [CharsetSpec] implementation for Windows-1256 (Arabic).
    public static enum Windows1256 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1256;
        }
    }

    /// [CharsetSpec] implementation for Windows-1257 (Baltic).
    public static enum Windows1257 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1257;
        }
    }

    /// [CharsetSpec] implementation for Windows-1258 (Vietnamese).
    public static enum Windows1258 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_1258;
        }
    }

    /// [CharsetSpec] implementation for Windows-874 (Thai).
    public static enum Windows874 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_WINDOWS_874;
        }
    }

    /// [CharsetSpec] implementation for ISO 8859-2 (Latin-2, Central European).
    public static enum Iso88592 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_2;
        }
    }

    /// [CharsetSpec] implementation for ISO 8859-7 (Greek).
    public static enum Iso88597 implements CharsetSpec {
        /// The sole instance.
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return CS_ISO_8859_7;
        }
    }
}
