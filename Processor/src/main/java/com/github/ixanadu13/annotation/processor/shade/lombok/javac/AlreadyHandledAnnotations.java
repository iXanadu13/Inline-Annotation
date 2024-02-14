package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate a handler is to be invoked for its marked annotation even if that annotation is already handled. Useful for cleanup handlers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlreadyHandledAnnotations {}
