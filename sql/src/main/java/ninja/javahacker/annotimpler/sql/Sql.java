package ninja.javahacker.annotimpler.sql;

import ninja.javahacker.annotimpler.sql.sqlfactories.StringSqlFactory;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = StringSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql {
    public String value();
}
