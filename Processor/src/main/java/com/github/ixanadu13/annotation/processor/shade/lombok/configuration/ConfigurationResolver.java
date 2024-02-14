package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

public interface ConfigurationResolver {
    <T> T resolve(ConfigurationKey<T> key);
}
