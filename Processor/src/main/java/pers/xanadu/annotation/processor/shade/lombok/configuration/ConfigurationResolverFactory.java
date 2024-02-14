package pers.xanadu.annotation.processor.shade.lombok.configuration;

import java.net.URI;

public interface ConfigurationResolverFactory {
    ConfigurationResolver createResolver(URI sourceLocation);
}
