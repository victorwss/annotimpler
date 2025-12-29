package ninja.javahacker.sqlplus.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SqlSource {
    public Class<? extends SqlFactory> factory();
    public boolean lazy();
}
