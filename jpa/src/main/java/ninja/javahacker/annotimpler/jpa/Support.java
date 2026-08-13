package ninja.javahacker.annotimpler.jpa;

import java.util.Locale;
import lombok.NonNull;

/// Enum for the values of the Open JPA property `openjpa.RuntimeUnenhancedClasses`.
/// @author Victor Williams Stafusa da Silva
public enum Support {

    /// Used to represent that no setting about the usage of enhanced classes should be defined.
    UNSPECIFIED,

    /// Used to represent that unenhanced classes should be supported at runtime.
    SUPPORTED,

    /// Used to represent that unenhanced classes should not be supported at runtime.
    /// Since the official documentation strongly recommends this, it was defined as default.
    UNSUPPORTED,

    /// Used to represent that unenhanced classes should be supported at runtime, but a warning should be emitted for them.
    WARN;

    /// The lowercase textual representation of this enum constant.
    @NonNull
    private final String asString;

    /// The code representing this enum constant for the `openjpa.RuntimeUnenhancedClasses` property.
    @NonNull
    private final String code;

    /// Creates a `Support` instance, deriving its `asString` and `code` from the enum constant's name.
    private Support() {
        this.asString = name().toLowerCase(Locale.ROOT);
        this.code = ordinal() == 0 ? "" : asString;
    }

    /// Returns `"unspecified"`, `"supported"`, `"unsupported"` or `"warn"`,
    /// depending on which elements of the enum `this` is.
    /// @return `"unspecified"`, `"supported"`, `"unsupported"` or `"warn"`.
    @NonNull
    @Override
    public String toString() {
        return asString;
    }

    /// Returns `""`, `"supported"`, `"unsupported"` or `"warn"`,
    /// depending on which elements of the enum `this` is.
    /// @return `""`, `"supported"`, `"unsupported"` or `"warn"`.
    @NonNull
    public String getCode() {
        return code;
    }
}
