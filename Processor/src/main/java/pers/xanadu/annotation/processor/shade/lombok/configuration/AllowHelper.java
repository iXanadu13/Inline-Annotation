package pers.xanadu.annotation.processor.shade.lombok.configuration;

import java.util.Collection;
import java.util.Collections;

public final class AllowHelper {
    private final static Collection<? extends ConfigurationKey<?>> ALLOWABLE = Collections.emptySet();

    private AllowHelper() {
        // Prevent instantiation
    }

    public static boolean isAllowable(ConfigurationKey<?> key) {
        return ALLOWABLE.contains(key);
    }
}
