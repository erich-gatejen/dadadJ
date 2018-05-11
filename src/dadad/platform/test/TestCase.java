package dadad.platform.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A test case annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestCase {

    enum Type {
        UNIT,
        COMPONENT,
        DATA,
        INTEGRATION
    }

    Type type() default Type.UNIT;

    int priority() default 1;

}
