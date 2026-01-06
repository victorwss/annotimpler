package ninja.javahacker.annotimpler.sql.meta;

import module java.base;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SqlSource {
    public Class<? extends SqlFactory> factory();
    public boolean lazy();
}
