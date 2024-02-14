package com.github.ixanadu13.annotation.processor.shade.lombok;

import java.util.regex.Pattern;

/**
 * Utility functions for validating potential java verifiers.
 */
public class JavaIdentifiers {
    private JavaIdentifiers() {}

    private static final LombokImmutableList<String> KEYWORDS = LombokImmutableList.of(
            "public", "private", "protected",
            "default", "switch", "case",
            "for", "do", "goto", "const", "strictfp", "while", "if", "else",
            "byte", "short", "int", "long", "float", "double", "void", "boolean", "char",
            "null", "false", "true",
            "continue", "break", "return", "instanceof",
            "synchronized", "volatile", "transient", "final", "static",
            "interface", "class", "extends", "implements", "throws",
            "throw", "catch", "try", "finally", "abstract", "assert",
            "enum", "import", "package", "native", "new", "super", "this");

    public static boolean isValidJavaIdentifier(String identifier) {
        if (identifier == null) return false;
        if (identifier.isEmpty()) return false;

        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) return false;
        for (int i = 1; i < identifier.length(); i++) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) return false;
        }

        return !isKeyword(identifier);
    }

    public static boolean isKeyword(String keyword) {
        return KEYWORDS.contains(keyword);
    }

    /** Matches any of the 8 primitive names, such as {@code boolean}. */
    private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile("^(?:boolean|byte|short|int|long|float|double|char)$");

    public static boolean isPrimitive(String typeName) {
        return PRIMITIVE_TYPE_NAME_PATTERN.matcher(typeName).matches();
    }


}
