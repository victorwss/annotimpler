package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

/// Specifies the strategy used for automatic schema generation or validation.
///
/// Used to set the property `jakarta.persistence.schema-generation.database.action`.
///
/// The `jakarta.persistence.schema-generation.database.action` property is used to specify the action taken
/// by the persistence provider when an application is deployed.
/// @see <a href=
/// "https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-intro/persistence-intro.html#_database_schema_creation">
///     Database Schema Creation</a>
/// @author Victor Williams Stafusa da Silva
public enum SchemaGenerationAction {

    /// Defines that the strategy for schema generation or validation is left unspecified. If the property is not set,
    /// the persistence provider will not create or drop any database artifacts.
    UNSPECIFIED(""),

    /// Defines that the strategy for schema generation or validation is that no schema creation or deletion will take place.
    NONE("none"),

    /// Defines that the strategy for schema generation or validation is that any artifacts in the database will be deleted
    /// on application deployment.
    DROP("drop"),

    /// Defines that the strategy for schema generation or validation is that the provider will create the database artifacts
    /// on application deployment. The artifacts will remain unchanged after application redeployment.
    CREATE("create"),

    /// Defines that the strategy for schema generation or validation is that any artifacts in the database will be deleted,
    /// and the provider will create the database artifacts on deployment.
    DROP_AND_CREATE("drop-and-create");

    /// The code representing this strategy, as used by the corresponding JPA provider property.
    @NonNull
    private final String code;

    /// Creates a `SchemaGenerationAction` with the given code.
    /// @param code The code representing this strategy; must not be `null`.
    /// @throws IllegalArgumentException If `code` is `null`.
    private SchemaGenerationAction(@NonNull String code) {
        this.code = code;
    }

    /// Returns `""`, `"none"`, `"drop"`, `"create"` or `"drop-and-create"`
    /// depending on which elements of the enum `this` is.
    /// @return `""`, `"none"`, `"drop"`, `"create"` or `"drop-and-create"`
    ///     depending on which elements of the enum `this` is.
    @NonNull
    public String getCode() {
        return code;
    }
}
