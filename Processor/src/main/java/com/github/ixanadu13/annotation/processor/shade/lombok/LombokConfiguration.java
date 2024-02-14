package com.github.ixanadu13.annotation.processor.shade.lombok;

import java.net.URI;
import java.util.Collections;

import com.github.ixanadu13.annotation.processor.shade.lombok.configuration.*;

public class LombokConfiguration {
    private static final ConfigurationResolver NULL_RESOLVER = new ConfigurationResolver() {
        @SuppressWarnings("unchecked") @Override public <T> T resolve(ConfigurationKey<T> key) {
            if (key.getType().isList()) return (T) Collections.emptyList();
            return null;
        }
    };

    private static FileSystemSourceCache cache = new FileSystemSourceCache();
    private static ConfigurationResolverFactory configurationResolverFactory;

    static {
        if (System.getProperty("lombok.disableConfig") != null) {
            configurationResolverFactory = new ConfigurationResolverFactory() {
                @Override public ConfigurationResolver createResolver(URI sourceLocation) {
                    return NULL_RESOLVER;
                }
            };
        }
        else {
            configurationResolverFactory = createFileSystemBubblingResolverFactory();
        }
    }

    private LombokConfiguration() {
        // prevent instantiation
    }

    public static void overrideConfigurationResolverFactory(ConfigurationResolverFactory crf) {
        configurationResolverFactory = crf == null ? createFileSystemBubblingResolverFactory() : crf;
    }

    static <T> T read(ConfigurationKey<T> key, AST<?, ?, ?> ast) {
        return read(key, ast.getAbsoluteFileLocation());
    }

    public static <T> T read(ConfigurationKey<T> key, URI sourceLocation) {
        return configurationResolverFactory.createResolver(sourceLocation).resolve(key);
    }

    private static ConfigurationResolverFactory createFileSystemBubblingResolverFactory() {
        final ConfigurationFileToSource fileToSource = cache.fileToSource(new ConfigurationParser(ConfigurationProblemReporter.CONSOLE));
        return new ConfigurationResolverFactory() {
            @Override public ConfigurationResolver createResolver(URI sourceLocation) {
                return new BubblingConfigurationResolver(cache.forUri(sourceLocation), fileToSource);
            }
        };
    }
}