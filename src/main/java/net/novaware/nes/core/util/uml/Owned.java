package net.novaware.nes.core.util.uml;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composition
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Owned {

    /**
     * (Optional) The class that owns the field
     *
     * <p> Defaults to the type that stores the association.
     *
     * @return class which owns the target field
     */
    Class<?> by() default void.class;
}
