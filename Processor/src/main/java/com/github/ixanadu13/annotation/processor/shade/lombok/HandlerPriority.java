package com.github.ixanadu13.annotation.processor.shade.lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to order the way handlers are run. Handlers with a lower priority value are run before handlers with higher priority values.
 * For example, {@code @Value} can cause the class to be marked final, and this affects the behaviour of {@code @EqualsAndHashCode}. By ensuring that
 * the handler for {@code @Value} always runs before the handler for {@code @EqualsAndHashCode}, the code is simpler: The {@code @EqualsAndHashCode} handler
 * does not have to check for the presence of a {@code @Value} annotation to determine whether to generate the {@code canEqual} method or not.
 * <p>
 * A new priority level can also be used to force a reset of the resolved model, i.e. to add generated methods to the symbol tables. Each platform implementation (javac, ecj, etc)
 * may have additional marker annotations required to indicate the need for the reset.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerPriority {
    int value();

    /**
     * This can be used to differentiate 2 handlers with the same value to be at a different handler priority anyway.
     * <strong>DO NOT USE THIS</strong> unless someone has been crowding out the numbers and there's no room left.
     */
    int subValue() default 0;
}
