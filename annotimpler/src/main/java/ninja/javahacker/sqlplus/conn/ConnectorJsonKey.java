package ninja.javahacker.sqlplus.conn;

import module java.base;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConnectorJsonKey {
    public String value();
}
