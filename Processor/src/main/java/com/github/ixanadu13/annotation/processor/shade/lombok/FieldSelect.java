package com.github.ixanadu13.annotation.processor.shade.lombok;

public class FieldSelect {
    private final String finalPart;

    public FieldSelect(String finalPart) {
        this.finalPart = finalPart;
    }

    public String getFinalPart() {
        return finalPart;
    }
}
