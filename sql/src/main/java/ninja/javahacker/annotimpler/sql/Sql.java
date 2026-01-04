package ninja.javahacker.annotimpler.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ninja.javahacker.annotimpler.sql.meta.SqlSource;
import ninja.javahacker.annotimpler.sql.sqlfactories.StringSqlFactory;

@SqlSource(factory = StringSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sql {
    public String value();
}
