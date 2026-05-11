package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = ResourceSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromResource {
    public String value();
    public Class<?> fromClass() default void.class;
    public Class<? extends CharsetSpec> encoding() default CharsetSpec.Utf8.class;
}
