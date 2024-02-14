package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.ixanadu13.annotation.processor.shade.lombok.SpiLoadUtil;

public interface ConfigurationKeysLoader {
    public class LoaderLoader {
        private static final AtomicBoolean alreadyLoaded = new AtomicBoolean(false);
        private LoaderLoader() {}

        public static void loadAllConfigurationKeys() {
            if (alreadyLoaded.get()) return;

            try {
                Class.forName(ConfigurationKeys.class.getName());
            } catch (Throwable ignore) {}

            try {
                Iterator<ConfigurationKeysLoader> iterator = SpiLoadUtil.findServices(ConfigurationKeysLoader.class, ConfigurationKeysLoader.class.getClassLoader()).iterator();
                while (iterator.hasNext()) {
                    try {
                        iterator.next();
                    } catch (Exception ignore) {}
                }
            } catch (IOException e) {
                throw new RuntimeException("Can't load config keys; services file issue.", e);
            } finally {
                alreadyLoaded.set(true);
            }
        }
    }
}