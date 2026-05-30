package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(FileSqlFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromFile {
    public String value();
    public ReadPolicy policy() default ReadPolicy.EVERY_TIME;
    public Class<? extends CharsetSpec> encoding() default CharsetSpec.Utf8.class;
}
