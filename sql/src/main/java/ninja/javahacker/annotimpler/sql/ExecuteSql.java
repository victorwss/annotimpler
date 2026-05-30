package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(ExecuteSqlImplementation.class)
public @interface ExecuteSql {
    public boolean acceptsZero() default false;
    public boolean acceptsMulti() default false;
    public boolean validate() default true;
}