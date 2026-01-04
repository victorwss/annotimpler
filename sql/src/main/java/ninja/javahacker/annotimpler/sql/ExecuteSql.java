package ninja.javahacker.annotimpler.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ninja.javahacker.annotimpler.core.ImplementedBy;
import ninja.javahacker.annotimpler.sql.sqlimpl.ExecuteSqlImplementation;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(ExecuteSqlImplementation.class)
public @interface ExecuteSql {
    public boolean aceitaZero() default false;
    public boolean aceitaMulti() default false;
}