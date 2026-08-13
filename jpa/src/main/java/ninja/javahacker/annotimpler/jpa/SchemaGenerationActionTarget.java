package ninja.javahacker.annotimpler.jpa;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/// Specifies whether scripts for creation and dropping of the tables should be automatically generated and where they should be stored.
///
/// Used to set the following properties:
/// - `jakarta.persistence.schema-generation.scripts.action`.
/// - `jakarta.persistence.schema-generation.scripts.create-target`.
/// - `jakarta.persistence.schema-generation.scripts.drop-target`.
/// @see #unspecified()
/// @see #none()
/// @see #drop(String)
/// @see #create(String)
/// @see #dropAndCreate(String, String)
/// @see <a href=
/// "https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-intro/persistence-intro.html#_database_schema_creation">
///     Database Schema Creation</a>
/// @author Victor Williams Stafusa da Silva
@Value
@SuppressWarnings("missing-explicit-ctor")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaGenerationActionTarget {

    /**
     * The set of scripts produced for table creation and droppings, if defined.
     * -- GETTER --
     * Provides what is the set of scripts used for table creation and droppings.
     * @return The set of scripts used for table creation and droppings, or an empty string ({@code ""}) if there is none defined.
     */
    // NOTE: Kept as a traditional block comment (not `///`) because Lombok's `-- GETTER --` javadoc-splitting
    // mechanism (used by this project's modified Lombok build) only recognizes classic `/** */` comment trees,
    // not JEP 467 `///` line comments; with `///` the whole comment stays attached to the field and the
    // generated getter ends up with no Javadoc at all.
    @NonNull
    private final String strategy;

    /**
     * The location for the produced script for table creation.
     * -- GETTER --
     * If an user-provided script should be created for creating database artifacts, this gives the target
     * script location relative to the root of the persistence unit. For example, {@code "META-INF/sql/some-create-script.sql"}.
     * @return The location of the user-provided script for table creation or an empty string ({@code ""}) if there is none.
     */
    // NOTE: Same Lombok `-- GETTER --` limitation as `strategy` above; kept as a traditional block comment.
    @NonNull
    private final String createScript;

    /**
     * The location for the produced script for table dropping.
     * -- GETTER --
     * If an user-provided script should be created for dropping database artifacts, this gives the target
     * script location relative to the root of the persistence unit. For example, {@code "META-INF/sql/some-drop-script.sql"}.
     * @return The location of the user-provided script for table droppings or an empty string ({@code ""}) if there is none.
     */
    // NOTE: Same Lombok `-- GETTER --` limitation as `strategy` above; kept as a traditional block comment.
    @NonNull
    private final String dropScript;

    /// Defines that the production of scripts for table creation and droppings is left unspecified.
    /// @return An object representing the strategy detailed above.
    public static SchemaGenerationActionTarget unspecified() {
        return new SchemaGenerationActionTarget("", "", "");
    }

    /// Defines that no production of scripts for table creation and dropping should happen.
    /// @return An object representing the strategy detailed above.
    public static SchemaGenerationActionTarget none() {
        return new SchemaGenerationActionTarget("none", "", "");
    }

    /// Defines that no scripts for table creation should be produced, but a script for table droppings should.
    /// @param dropScript The location where the script to drop tables relative to the root of the persistence unit should be stored.
    ///     For example, `"META-INF/sql/some-create-script.sql"`.
    /// @return An object representing the strategy detailed above.
    public static SchemaGenerationActionTarget drop(@NonNull String dropScript) {
        return new SchemaGenerationActionTarget("drop", "", dropScript);
    }

    /// Defines that no scripts for table droppings should be produced, but a script for table creation should.
    /// @param createScript The location where the script to create tables relative to the root of the persistence unit should be stored.
    ///     For example, `"META-INF/sql/some-drop-script.sql"`.
    /// @return An object representing the strategy detailed above.
    public static SchemaGenerationActionTarget create(@NonNull String createScript) {
        return new SchemaGenerationActionTarget("create", createScript, "");
    }

    /// Defines that scripts for both table creation and droppings should be produced.
    /// @param dropScript The location where the script to drop tables relative to the root of the persistence unit should be stored.
    ///     For example, `"META-INF/sql/some-create-script.sql"`.
    /// @param createScript The location where the script to create tables relative to the root of the persistence unit should be stored.
    ///     For example, `"META-INF/sql/some-drop-script.sql"`.
    /// @return An object representing the strategy detailed above.
    public static SchemaGenerationActionTarget dropAndCreate(@NonNull String createScript, @NonNull String dropScript) {
        return new SchemaGenerationActionTarget("drop-and-create", createScript, dropScript);
    }
}
