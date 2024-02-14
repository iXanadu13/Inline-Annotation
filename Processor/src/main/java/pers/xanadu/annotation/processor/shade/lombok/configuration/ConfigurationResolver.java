package pers.xanadu.annotation.processor.shade.lombok.configuration;

public interface ConfigurationResolver {
    <T> T resolve(ConfigurationKey<T> key);
}
