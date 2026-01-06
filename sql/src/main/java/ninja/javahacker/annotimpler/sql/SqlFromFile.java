package ninja.javahacker.annotimpler.sql;

import ninja.javahacker.annotimpler.sql.sqlfactories.FileSqlFactory;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = FileSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromFile {
    public String value();
}
