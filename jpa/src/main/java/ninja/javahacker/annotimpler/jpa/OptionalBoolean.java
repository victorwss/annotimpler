package ninja.javahacker.annotimpler.jpa;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Locale;
import lombok.NonNull;

/// An enum that defines the values true, false, or unspecified.
/// @author Victor Williams Stafusa da Silva
public enum OptionalBoolean {

    /// Used to represent that some setting was left undefined with neither [#TRUE] nor [#FALSE] being specified as its value.
    UNSPECIFIED,

    /// Used to represent that some setting was defined as being false.
    FALSE,

    /// Used to represent that some setting was defined as being true.
    TRUE;

    /// The lowercase textual representation of this enum constant.
    @NonNull
    private final String asString;

    /// The code representing this enum constant for provider properties.
    @NonNull
    private final String code;

    /// Creates an `OptionalBoolean` instance, deriving its `asString` and `code` from the enum constant's name.
    private OptionalBoolean() {
        this.asString = name().toLowerCase(Locale.ROOT);
        this.code = ordinal() == 0 ? "" : asString;
    }

    /// Returns `"unspecified"`, `"true"` or `"false"` depending on which elements of the enum `this` is.
    /// @return `"unspecified"`, `"true"` or `"false"`.
    @NonNull
    @Override
    public String toString() {
        return asString;
    }

    /// Returns `""`, `"true"` or `"false"` depending on which elements of the enum `this` is.
    /// @return `""`, `"true"` or `"false"`.
    @NonNull
    public String getCode() {
        return code;
    }

    /// Converts a `boolean` to either [#TRUE] or [#FALSE]. Never returns [#UNSPECIFIED].
    /// @param b The value to be convert.
    /// @return The converted value.
    @NonNull
    public static OptionalBoolean from(boolean b) {
        return b ? TRUE : FALSE;
    }

    /// Converts a [Boolean] to either [#TRUE] or [#FALSE] or even [#UNSPECIFIED] if converting from `null`.
    /// @param b The value to be convert; may be `null`.
    /// @return The converted value.
    @NonNull
    public static OptionalBoolean from(@Nullable Boolean b) {
        return b == null ? UNSPECIFIED : b ? TRUE : FALSE;
    }
}
