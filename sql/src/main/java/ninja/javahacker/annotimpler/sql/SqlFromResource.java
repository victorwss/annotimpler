package ninja.javahacker.annotimpler.sql;

import ninja.javahacker.annotimpler.sql.sqlfactories.ResourceSqlFactory;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = ResourceSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromResource {
    public String value();
}
