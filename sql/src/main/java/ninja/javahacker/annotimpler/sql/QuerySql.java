package ninja.javahacker.annotimpler.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ninja.javahacker.annotimpler.core.ImplementedBy;
import ninja.javahacker.annotimpler.sql.sqlimpl.QuerySqlImplementation;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(QuerySqlImplementation.class)
public @interface QuerySql {
    public int[] campos() default {};
}
