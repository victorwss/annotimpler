package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(QuerySqlImplementation.class)
public @interface QuerySql {
    public int[] fields() default {};
    public SqlPreValidation validate() default SqlPreValidation.ON_LOAD;
}
