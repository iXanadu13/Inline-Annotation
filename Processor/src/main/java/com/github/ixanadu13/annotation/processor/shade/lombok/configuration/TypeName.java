package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import com.github.ixanadu13.annotation.processor.shade.lombok.JavaIdentifiers;

public final class TypeName implements ConfigurationValueType {
    private final String name;

    private TypeName(String name) {
        this.name = name;
    }

    public static TypeName valueOf(String name) {
        if (name == null || name.trim().isEmpty()) return null;

        String trimmedName = name.trim();
        for (String identifier : trimmedName.split("\\.")) {
            if (!JavaIdentifiers.isValidJavaIdentifier(identifier)) throw new IllegalArgumentException("Invalid type name " + trimmedName + " (part " + identifier + ")");
        }
        return new TypeName(trimmedName);
    }

    public static String description() {
        return "type-name";
    }

    public static String exampleValue() {
        return "<fully.qualified.Type>";
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof TypeName)) return false;
        return name.equals(((TypeName) obj).name);
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
