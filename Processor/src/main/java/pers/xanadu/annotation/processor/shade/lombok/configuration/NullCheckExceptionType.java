package pers.xanadu.annotation.processor.shade.lombok.configuration;

import pers.xanadu.annotation.processor.shade.lombok.LombokImmutableList;

@ExampleValueString("[NullPointerException | IllegalArgumentException | Assertion | JDK | Guava]")
public enum NullCheckExceptionType {
    ILLEGAL_ARGUMENT_EXCEPTION {
        @Override public String getExceptionType() {
            return "java.lang.IllegalArgumentException";
        }

        @Override public LombokImmutableList<String> getMethod() {
            return null;
        }
    },
    NULL_POINTER_EXCEPTION {
        @Override public String getExceptionType() {
            return "java.lang.NullPointerException";
        }

        @Override public LombokImmutableList<String> getMethod() {
            return null;
        }
    },
    ASSERTION {
        @Override public String getExceptionType() {
            return null;
        }

        @Override public LombokImmutableList<String> getMethod() {
            return null;
        }
    },
    JDK {
        @Override public String getExceptionType() {
            return null;
        }

        @Override public LombokImmutableList<String> getMethod() {
            return METHOD_JDK;
        }
    },
    GUAVA {
        @Override public String getExceptionType() {
            return null;
        }

        @Override public LombokImmutableList<String> getMethod() {
            return METHOD_GUAVA;
        }
    };

    private static final LombokImmutableList<String> METHOD_JDK = LombokImmutableList.of("java", "util", "Objects", "requireNonNull");
    private static final LombokImmutableList<String> METHOD_GUAVA = LombokImmutableList.of("com", "google", "common", "base", "Preconditions", "checkNotNull");

    public String toExceptionMessage(String fieldName, String customMessage) {
        if (customMessage == null) return fieldName + " is marked non-null but is null";
        return customMessage.replace("%s", fieldName);
    }

    public abstract String getExceptionType();

    public abstract LombokImmutableList<String> getMethod();
}