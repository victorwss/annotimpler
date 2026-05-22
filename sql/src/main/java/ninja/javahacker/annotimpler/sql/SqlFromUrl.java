package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@SqlSource(factory = UrlSqlFactory.class, lazy = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlFromUrl {
    public String value();
    public ReadPolicy policy() default ReadPolicy.EVERY_TIME;
    public boolean getEncodingFromHeaders() default true;
    public Class<? extends CharsetSpec> fallbackEncoding() default CharsetSpec.Utf8.class;
}
