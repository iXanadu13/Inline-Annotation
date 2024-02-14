package pers.xanadu.annotation.processor.shade.lombok.configuration;

interface ConfigurationValueParser {
    Object parse(String value);
    String description();
    String exampleValue();
}
