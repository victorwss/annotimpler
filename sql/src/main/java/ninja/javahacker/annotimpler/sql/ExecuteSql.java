package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(ExecuteSqlImplementation.class)
public @interface ExecuteSql {
    public boolean aceitaZero() default false;
    public boolean aceitaMulti() default false;
}