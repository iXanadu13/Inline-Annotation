package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import com.github.ixanadu13.annotation.processor.shade.lombok.JavaIdentifiers;

public final class IdentifierName implements ConfigurationValueType {
    private final String name;

    private IdentifierName(String name) {
        this.name = name;
    }

    public static IdentifierName valueOf(String name) {
        if (name == null || name.trim().isEmpty()) return null;

        String trimmedName = name.trim();
        if (!JavaIdentifiers.isValidJavaIdentifier(trimmedName)) throw new IllegalArgumentException("Invalid identifier " + trimmedName);
        return new IdentifierName(trimmedName);
    }

    public static String description() {
        return "identifier-name";
    }

    public static String exampleValue() {
        return "<javaIdentifier>";
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof IdentifierName)) return false;
        return name.equals(((IdentifierName) obj).name);
    }

    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public char[] getCharArray() {
        return name.toCharArray();
    }
}
