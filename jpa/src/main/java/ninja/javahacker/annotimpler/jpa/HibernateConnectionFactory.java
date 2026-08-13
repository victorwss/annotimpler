package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

import module java.base;
import module jakarta.persistence;

/// Immutable [EntityManagerSupplier] configured for Hibernate-specific settings.
///
/// @param persistenceUnitName The JPA persistence-unit name.
/// @param url JDBC URL.
/// @param user Database user.
/// @param password Database password.
/// @param schema Default schema name (`hibernate.default_schema`).
/// @param dialect Optional Hibernate dialect class name.
/// @param jtaPlatform Optional Hibernate JTA platform class name.
/// @param showSql Whether to enable SQL logging.
/// @param formatSql Whether to format SQL output.
/// @param useSqlComments Whether to include SQL comments.
/// @param multipleLinesCommands Whether to use Hibernate's multi-line SQL script extractor.
/// @param newGeneratorMappings Whether to use Hibernate's new generator mappings.
/// @param extras Extra provider properties merged last.
public record HibernateConnectionFactory(
        @NonNull String persistenceUnitName,
        @NonNull String url,
        @NonNull String user,
        @NonNull String password,
        @NonNull String schema,
        @NonNull Optional<String> dialect,
        @NonNull Optional<String> jtaPlatform,
        @NonNull OptionalBoolean showSql,
        @NonNull OptionalBoolean formatSql,
        @NonNull OptionalBoolean useSqlComments,
        boolean multipleLinesCommands,
        @NonNull OptionalBoolean newGeneratorMappings,
        @NonNull Map<String, String> extras
) implements EntityManagerSupplier
{
    /// A `HibernateConnectionFactory` with all fields left unspecified.
    private static final HibernateConnectionFactory STD = new HibernateConnectionFactory(
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
            Optional.empty(),
            OptionalBoolean.UNSPECIFIED,
            OptionalBoolean.UNSPECIFIED,
            OptionalBoolean.UNSPECIFIED,
            true,
            OptionalBoolean.UNSPECIFIED,
            Map.of()
    );

    /// Creates a `HibernateConnectionFactory` with the given field values.
    /// @param persistenceUnitName The JPA persistence-unit name.
    /// @param url JDBC URL.
    /// @param user Database user.
    /// @param password Database password.
    /// @param schema Default schema name (`hibernate.default_schema`).
    /// @param dialect Optional Hibernate dialect class name.
    /// @param jtaPlatform Optional Hibernate JTA platform class name.
    /// @param showSql Whether to enable SQL logging.
    /// @param formatSql Whether to format SQL output.
    /// @param useSqlComments Whether to include SQL comments.
    /// @param multipleLinesCommands Whether to use Hibernate's multi-line SQL script extractor.
    /// @param newGeneratorMappings Whether to use Hibernate's new generator mappings.
    /// @param extras Extra provider properties merged last.
    /// @throws IllegalArgumentException If any argument annotated as such is `null`.
    public HibernateConnectionFactory {}

    /// Returns a `HibernateConnectionFactory` with all fields left unspecified.
    /// @return A `HibernateConnectionFactory` with all fields left unspecified.
    @NonNull
    public static HibernateConnectionFactory std() {
        return STD;
    }

    /// Builds the map of Hibernate/JPA provider properties derived from this configuration.
    /// @return The derived map.
    @NonNull
    private Map<String, String> properties() {
        var out = new HashMap<String, String>();
        out.put("jakarta.persistence.jdbc.url", url);
        out.put("jakarta.persistence.jdbc.user", user);
        out.put("jakarta.persistence.jdbc.password", password);

        if (!schema.isEmpty()) out.put("hibernate.default_schema", schema);
        dialect.ifPresent(v -> out.put("hibernate.dialect", v));
        jtaPlatform.ifPresent(v -> out.put("hibernate.transaction.jta.platform", v));

        var show = showSql.getCode();
        if (!show.isEmpty()) out.put("hibernate.show_sql", show);
        var format = formatSql.getCode();
        if (!format.isEmpty()) out.put("hibernate.format_sql", format);
        var comments = useSqlComments.getCode();
        if (!comments.isEmpty()) out.put("hibernate.use_sql_comments", comments);
        var mappings = newGeneratorMappings.getCode();
        if (!mappings.isEmpty()) out.put("hibernate.id.new_generator_mappings", mappings);

        if (multipleLinesCommands) {
            out.put("hibernate.hbm2ddl.import_files_sql_extractor", "org.hibernate.tool.schema.internal.script.MultiLineSqlScriptExtractor");
        }

        out.putAll(extras);
        return Map.copyOf(out);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedEntityManager get() {
        return JpaEntityManagers.createExtended(HibernateConnectionFactory::shouldTryToReconnect, persistenceUnitName, properties());
    }

    private static boolean shouldTryToReconnect(@NonNull RuntimeException e) {
        return isJDBCConnectionException(e.getClass()) && "Unable to acquire JDBC Connection".equals(e.getMessage());
    }

    private static boolean isJDBCConnectionException(@NonNull Class<?> e) {
        if (e == Object.class) return false;
        if ("org.hibernate.exception.JDBCConnectionException".equals(e.getName())) return true;
        var sup = e.getSuperclass();
        return isJDBCConnectionException(sup);
    }

    /// Returns a copy of this connection factory with the persistence-unit name replaced by the given value.
    /// @param persistenceUnitName The new JPA persistence-unit name.
    /// @return A new connection factory with the updated persistence-unit name.
    /// @throws IllegalArgumentException If `persistenceUnitName` is `null`.
    @NonNull
    public HibernateConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the JDBC URL replaced by the given value.
    /// @param url The new JDBC URL.
    /// @return A new connection factory with the updated URL.
    /// @throws IllegalArgumentException If `url` is `null`.
    @NonNull
    public HibernateConnectionFactory withUrl(@NonNull String url) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the authentication credentials replaced by
    /// the given username and password.
    /// @param user The new database username.
    /// @param password The new database password.
    /// @return A new connection factory with the updated credentials.
    /// @throws IllegalArgumentException If either argument is `null`.
    @NonNull
    public HibernateConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the default schema name replaced by the given value.
    /// @param schema The new default schema name (`hibernate.default_schema`).
    /// @return A new connection factory with the updated schema.
    /// @throws IllegalArgumentException If `schema` is `null`.
    @NonNull
    public HibernateConnectionFactory withSchema(@NonNull String schema) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the Hibernate dialect class name replaced by the given value.
    /// @param dialect The new Hibernate dialect class name.
    /// @return A new connection factory with the updated dialect.
    /// @throws IllegalArgumentException If `dialect` is `null`.
    @NonNull
    public HibernateConnectionFactory withDialect(@NonNull String dialect) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, Optional.of(dialect), jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the Hibernate dialect class name left unspecified.
    /// @return A new connection factory with no dialect specified.
    @NonNull
    public HibernateConnectionFactory withNoDialect() {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, Optional.empty(), jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the Hibernate JTA platform class name replaced by the given value.
    /// @param jtaPlatform The new Hibernate JTA platform class name.
    /// @return A new connection factory with the updated JTA platform.
    /// @throws IllegalArgumentException If `jtaPlatform` is `null`.
    @NonNull
    public HibernateConnectionFactory withJtaPlatform(@NonNull String jtaPlatform) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, Optional.of(jtaPlatform), showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the Hibernate JTA platform class name left unspecified.
    /// @return A new connection factory with no JTA platform specified.
    @NonNull
    public HibernateConnectionFactory withNoJtaPlatform() {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, Optional.empty(), showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the SQL logging setting replaced by the given value.
    /// @param showSql The new setting for whether to enable SQL logging.
    /// @return A new connection factory with the updated SQL logging setting.
    /// @throws IllegalArgumentException If `showSql` is `null`.
    @NonNull
    public HibernateConnectionFactory withShowSql(@NonNull OptionalBoolean showSql) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the SQL logging setting replaced by the given value.
    /// @param showSql The new setting for whether to enable SQL logging.
    /// @return A new connection factory with the updated SQL logging setting.
    @NonNull
    public HibernateConnectionFactory withShowSql(boolean showSql) {
        return withShowSql(OptionalBoolean.from(showSql));
    }

    /// Returns a copy of this connection factory with the SQL formatting setting replaced by the given value.
    /// @param formatSql The new setting for whether to format SQL output.
    /// @return A new connection factory with the updated SQL formatting setting.
    /// @throws IllegalArgumentException If `formatSql` is `null`.
    @NonNull
    public HibernateConnectionFactory withFormatSql(@NonNull OptionalBoolean formatSql) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the SQL formatting setting replaced by the given value.
    /// @param formatSql The new setting for whether to format SQL output.
    /// @return A new connection factory with the updated SQL formatting setting.
    @NonNull
    public HibernateConnectionFactory withFormatSql(boolean formatSql) {
        return withFormatSql(OptionalBoolean.from(formatSql));
    }

    /// Returns a copy of this connection factory with the SQL comments setting replaced by the given value.
    /// @param useSqlComments The new setting for whether to include SQL comments.
    /// @return A new connection factory with the updated SQL comments setting.
    /// @throws IllegalArgumentException If `useSqlComments` is `null`.
    @NonNull
    public HibernateConnectionFactory withUseSqlComments(@NonNull OptionalBoolean useSqlComments) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the SQL comments setting replaced by the given value.
    /// @param useSqlComments The new setting for whether to include SQL comments.
    /// @return A new connection factory with the updated SQL comments setting.
    @NonNull
    public HibernateConnectionFactory withUseSqlComments(boolean useSqlComments) {
        return withUseSqlComments(OptionalBoolean.from(useSqlComments));
    }

    /// Returns a copy of this connection factory with the multi-line SQL script extractor setting replaced by the given value.
    /// @param multipleLinesCommands The new setting for whether to use Hibernate's multi-line SQL script extractor.
    /// @return A new connection factory with the updated setting.
    @NonNull
    public HibernateConnectionFactory withMultipleLinesCommands(boolean multipleLinesCommands) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the new-generator-mappings setting replaced by the given value.
    /// @param newGeneratorMappings The new setting for whether to use Hibernate's new generator mappings.
    /// @return A new connection factory with the updated setting.
    /// @throws IllegalArgumentException If `newGeneratorMappings` is `null`.
    @NonNull
    public HibernateConnectionFactory withNewGeneratorMappings(@NonNull OptionalBoolean newGeneratorMappings) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    /// Returns a copy of this connection factory with the new-generator-mappings setting replaced by the given value.
    /// @param newGeneratorMappings The new setting for whether to use Hibernate's new generator mappings.
    /// @return A new connection factory with the updated setting.
    @NonNull
    public HibernateConnectionFactory withNewGeneratorMappings(boolean newGeneratorMappings) {
        return withNewGeneratorMappings(OptionalBoolean.from(newGeneratorMappings));
    }

    /// Returns a copy of this connection factory with the extra provider properties replaced by the given map.
    /// @param extras The new extra provider properties merged last.
    /// @return A new connection factory with the updated extra properties.
    /// @throws IllegalArgumentException If `extras` is `null`.
    @NonNull
    public HibernateConnectionFactory withExtras(@NonNull Map<String, String> extras) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, Map.copyOf(extras)
        );
    }
}
