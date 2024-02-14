package com.github.ixanadu13.annotation.processor.shade.lombok;

/**
 * Represents an AccessLevel. Used e.g. to specify the access level for generated methods and fields.
 */
public enum AccessLevel {
    PUBLIC, MODULE, PROTECTED, PACKAGE, PRIVATE,
    /** Represents not generating anything or the complete lack of a method. */
    NONE;
}