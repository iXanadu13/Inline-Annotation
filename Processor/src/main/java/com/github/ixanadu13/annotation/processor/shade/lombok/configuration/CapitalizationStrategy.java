package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

/** Used for lombok configuration to determine how to transform field names when turning them into accessor method names and vice versa. */
public enum CapitalizationStrategy {
    BASIC {
        @Override public String capitalize(String in) {
            if (in.length() == 0) return in;
            char first = in.charAt(0);
            if (!Character.isLowerCase(first)) return in;
            boolean useUpperCase = in.length() > 2 &&
                    (Character.isTitleCase(in.charAt(1)) || Character.isUpperCase(in.charAt(1)));
            return (useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first)) + in.substring(1);
        }
    },
    BEANSPEC {
        @Override public String capitalize(String in) {
            if (in.length() == 0) return in;
            char first = in.charAt(0);
            if (!Character.isLowerCase(first) || (in.length() > 1 && Character.isUpperCase(in.charAt(1)))) return in;
            boolean useUpperCase = in.length() > 2 && Character.isTitleCase(in.charAt(1));
            return (useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first)) + in.substring(1);
        }
    },
    ;

    public static CapitalizationStrategy defaultValue() {
        return BASIC;
    }

    public abstract String capitalize(String in);
}
