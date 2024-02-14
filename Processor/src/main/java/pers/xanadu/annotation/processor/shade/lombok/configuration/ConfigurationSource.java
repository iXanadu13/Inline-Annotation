package pers.xanadu.annotation.processor.shade.lombok.configuration;

import java.util.List;

public interface ConfigurationSource {

    Result resolve(ConfigurationKey<?> key);
    List<ConfigurationFile> imports();

    public static final class Result {
        private final Object value;
        private final boolean authoritative;

        public Result(Object value, boolean authoritative) {
            this.value = value;
            this.authoritative = authoritative;
        }

        public Object getValue() {
            return value;
        }

        public boolean isAuthoritative() {
            return authoritative;
        }

        @Override public String toString() {
            return String.valueOf(value) + (authoritative ? " (set)" : " (delta)");
        }
    }

    public static final class ListModification {
        private final Object value;
        private final boolean added;

        public ListModification(Object value, boolean added) {
            this.value = value;
            this.added = added;
        }

        public Object getValue() {
            return value;
        }

        public boolean isAdded() {
            return added;
        }
    }
}
