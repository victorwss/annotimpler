package ninja.javahacker.annotimpler.sql.conn;

import module java.base;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConnectorJsonKey {
    public String value();
}
