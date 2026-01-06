package ninja.javahacker.annotimpler.sql;

import ninja.javahacker.annotimpler.sql.sqlfactories.UrlSqlFactory;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = UrlSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromUrl {
    public String value();
}
