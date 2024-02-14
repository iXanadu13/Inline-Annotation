package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.net.URI;

public interface ConfigurationResolverFactory {
    ConfigurationResolver createResolver(URI sourceLocation);
}
