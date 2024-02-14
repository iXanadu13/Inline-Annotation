package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated handler as needing a reset on the resolved model (resulting in generated methods' signatures from becoming part of the symbol table and such) before
 * the priority level of this handler is reached.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResolutionResetNeeded {

}