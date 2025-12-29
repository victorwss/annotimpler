package ninja.javahacker.sqlplus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ninja.javahacker.annotimpler.ImplementedBy;
import ninja.javahacker.sqlplus.sqlimpl.GenerateSqlImplementation;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ImplementedBy(GenerateSqlImplementation.class)
public @interface GenerateSql {
}