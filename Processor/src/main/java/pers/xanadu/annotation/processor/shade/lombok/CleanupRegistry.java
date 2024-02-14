package pers.xanadu.annotation.processor.shade.lombok;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CleanupRegistry {
    private static final class CleanupKey {
        private final String key;
        private final Object target;

        CleanupKey(String key, Object target) {
            this.key = key;
            this.target = target;
        }

        @Override public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof CleanupKey)) return false;
            CleanupKey o = (CleanupKey) other;
            if (!key.equals(o.key)) return false;
            return target == o.target;
        }

        @Override public int hashCode() {
            return 109 * System.identityHashCode(target) + key.hashCode();
        }
    }

    private final ConcurrentMap<CleanupKey, CleanupTask> tasks = new ConcurrentHashMap<CleanupKey, CleanupTask>();

    public void registerTask(String key, Object target, CleanupTask task) {
        CleanupKey ck = new CleanupKey(key, target);
        tasks.putIfAbsent(ck, task);
    }

    public void run() {
        for (CleanupTask task : tasks.values()) {
            task.cleanup();
        }
        tasks.clear();
    }
}
