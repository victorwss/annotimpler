package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface CharsetSpec {

    public Charset get();

    public static class BadCharsetSpecException extends IOException {

        private static final long serialVersionUID = 1L;

        public BadCharsetSpecException(@NonNull Throwable cause) {
            List.of(cause);
            super(cause);
        }
    }

    public static Charset from(@NonNull Class<? extends CharsetSpec> k) throws IOException {
        try {
            return MagicFactory.of(k).create().get();
        } catch (Throwable e) {
            throw new BadCharsetSpecException(e);
        }
    }

    public static enum Utf8 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_8;
        }
    }

    public static enum Ascii implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.US_ASCII;
        }
    }

    public static enum Iso88591 implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.ISO_8859_1;
        }
    }

    public static enum Utf16_LE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_16LE;
        }
    }

    public static enum Utf16_BE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_16BE;
        }
    }

    public static enum Utf16_Bom implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_16;
        }
    }

    public static enum Utf32_LE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_32LE;
        }
    }

    public static enum Utf32_BE implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_32BE;
        }
    }

    public static enum Utf32_Bom implements CharsetSpec {
        @Creator
        INSTANCE;

        @Override
        public Charset get() {
            return StandardCharsets.UTF_32;
        }
    }
}
