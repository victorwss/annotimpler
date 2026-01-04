package ninja.javahacker.annotimpler.magicfactory;

import module java.base;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Creator {
}
