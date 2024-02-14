package com.github.ixanadu13.annotation.processor.shade.lombok;

public class ClassLiteral {
    private final String className;

    public ClassLiteral(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
