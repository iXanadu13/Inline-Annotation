package com.github.ixanadu13.annotation.processor.shade.lombok;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LombokInternalAliasing {
    public static final Map<String, String> ALIASES;
    public static final Map<String, Collection<String>> REVERSE_ALIASES;

    /**
     * Provide a fully qualified name (FQN), and the canonical version of this is returned.
     */
    public static String processAliases(String in) {
        if (in == null) return null;
        String ret = ALIASES.get(in);
        return ret == null ? in : ret;
    }

    static {
        Map<String, String> m1 = new HashMap<String, String>();
        m1.put("lombok.experimental.Value", "lombok.Value");
        m1.put("lombok.experimental.Builder", "lombok.Builder");
        m1.put("lombok.experimental.var", "lombok.var");
        m1.put("lombok.Delegate", "lombok.experimental.Delegate");
        m1.put("lombok.experimental.Wither", "lombok.With");
        ALIASES = Collections.unmodifiableMap(m1);

        Map<String, Collection<String>> m2 = new HashMap<String, Collection<String>>();
        for (Map.Entry<String, String> e : m1.entrySet()) {
            Collection<String> c = m2.get(e.getValue());
            if (c == null) {
                m2.put(e.getValue(), Collections.singleton(e.getKey()));
            } else if (c.size() == 1) {
                Collection<String> newC = new ArrayList<String>(2);
                newC.addAll(c);
                m2.put(e.getValue(), c);
            } else {
                c.add(e.getKey());
            }
        }
        for (Map.Entry<String, Collection<String>> e : m2.entrySet()) {
            Collection<String> c = e.getValue();
            if (c.size() > 1) e.setValue(Collections.unmodifiableList((ArrayList<String>) c));
        }
        REVERSE_ALIASES = Collections.unmodifiableMap(m2);
    }
}
