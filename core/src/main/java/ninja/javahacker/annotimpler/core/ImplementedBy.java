package ninja.javahacker.annotimpler.core;

import module java.base;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImplementedBy {
    public Class<? extends Implementation> value();
}
