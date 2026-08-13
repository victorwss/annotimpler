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

    public HibernateConnectionFactory {}

    @NonNull
    public static HibernateConnectionFactory std() {
        return STD;
    }

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

    @NonNull
    public HibernateConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withUrl(@NonNull String url) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withSchema(@NonNull String schema) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withDialect(@NonNull String dialect) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, Optional.of(dialect), jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withNoDialect() {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, Optional.empty(), jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withJtaPlatform(@NonNull String jtaPlatform) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, Optional.of(jtaPlatform), showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withNoJtaPlatform() {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, Optional.empty(), showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withShowSql(@NonNull OptionalBoolean showSql) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withShowSql(boolean showSql) {
        return withShowSql(OptionalBoolean.from(showSql));
    }

    @NonNull
    public HibernateConnectionFactory withFormatSql(@NonNull OptionalBoolean formatSql) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withFormatSql(boolean formatSql) {
        return withFormatSql(OptionalBoolean.from(formatSql));
    }

    @NonNull
    public HibernateConnectionFactory withUseSqlComments(@NonNull OptionalBoolean useSqlComments) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withUseSqlComments(boolean useSqlComments) {
        return withUseSqlComments(OptionalBoolean.from(useSqlComments));
    }

    @NonNull
    public HibernateConnectionFactory withMultipleLinesCommands(boolean multipleLinesCommands) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withNewGeneratorMappings(@NonNull OptionalBoolean newGeneratorMappings) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, extras
        );
    }

    @NonNull
    public HibernateConnectionFactory withNewGeneratorMappings(boolean newGeneratorMappings) {
        return withNewGeneratorMappings(OptionalBoolean.from(newGeneratorMappings));
    }

    @NonNull
    public HibernateConnectionFactory withExtras(@NonNull Map<String, String> extras) {
        return new HibernateConnectionFactory(
                persistenceUnitName, url, user, password, schema, dialect, jtaPlatform, showSql, formatSql, useSqlComments,
                multipleLinesCommands, newGeneratorMappings, Map.copyOf(extras)
        );
    }
}
