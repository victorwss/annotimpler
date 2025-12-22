package ninja.javahacker.sqlplus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ninja.javahacker.sqlplus.meta.SqlSource;
import ninja.javahacker.sqlplus.sqlfactories.FileSqlFactory;

@SqlSource(factory = FileSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromFile {
    public String value();
}
