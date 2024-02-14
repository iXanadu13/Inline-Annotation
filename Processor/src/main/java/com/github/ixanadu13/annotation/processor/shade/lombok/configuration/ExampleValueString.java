package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a configuration key has an enum type, then the 'example values' string is built up by just joining all enum keys together with a bar separator, but you
 * can add this annotation to the enum type to override this string.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExampleValueString {
    String value();
}