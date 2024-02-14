package pers.xanadu.annotation.processor.shade.lombok.configuration;

/**
 * If a type used in {@link ConfigurationKey} type argument implements this interface,
 * it is expected to provide the following three static methods:
 * <ul>
 * <li><code>public static SELF valueOf(String value)</code>
 * <li><code>public static String description()</code>
 * <li><code>public static String exampleValue()</code>
 * </ul>
 * None of them should throw checked exceptions.
 * Based on these methods, an instance of {@link ConfigurationValueParser} is created
 * and used by the configuration system.
 */
public interface ConfigurationValueType {
}
