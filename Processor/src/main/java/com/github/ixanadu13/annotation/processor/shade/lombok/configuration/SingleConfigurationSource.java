package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SingleConfigurationSource implements ConfigurationSource {
    private final Map<ConfigurationKey<?>, Result> values;
    private final List<ConfigurationFile> imports;

    public static ConfigurationSource parse(ConfigurationFile context, ConfigurationParser parser) {
        final Map<ConfigurationKey<?>, Result> values = new HashMap<ConfigurationKey<?>, Result>();
        final List<ConfigurationFile> imports = new ArrayList<ConfigurationFile>();
        ConfigurationParser.Collector collector = new ConfigurationParser.Collector() {
            @Override public void addImport(ConfigurationFile importFile, ConfigurationFile context, int lineNumber) {
                imports.add(importFile);
            }

            @Override public void clear(ConfigurationKey<?> key, ConfigurationFile context, int lineNumber) {
                values.put(key, new Result(null, true));
            }

            @Override public void set(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
                values.put(key, new Result(value, true));
            }

            @Override public void add(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
                modifyList(key, value, true);
            }

            @Override public void remove(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
                modifyList(key, value, false);
            }

            @SuppressWarnings("unchecked")
            private void modifyList(ConfigurationKey<?> key, Object value, boolean add) {
                Result result = values.get(key);
                List<ListModification> list;
                if (result == null || result.getValue() == null) {
                    list = new ArrayList<ConfigurationSource.ListModification>();
                    values.put(key, new Result(list, result != null));
                } else {
                    list = (List<ListModification>) result.getValue();
                }
                list.add(new ListModification(value, add));
            }
        };
        parser.parse(context, collector);
        return new SingleConfigurationSource(values, imports);
    }

    private SingleConfigurationSource(Map<ConfigurationKey<?>, Result> values, List<ConfigurationFile> imports) {
        this.values = new HashMap<ConfigurationKey<?>, Result>();
        for (Entry<ConfigurationKey<?>, Result> entry : values.entrySet()) {
            Result result = entry.getValue();
            if (result.getValue() instanceof List<?>) {
                this.values.put(entry.getKey(), new Result(Collections.unmodifiableList((List<?>) result.getValue()), result.isAuthoritative()));
            } else {
                this.values.put(entry.getKey(), result);
            }
        }
        this.imports = Collections.unmodifiableList(imports);
    }

    @Override
    public Result resolve(ConfigurationKey<?> key) {
        return values.get(key);
    }

    @Override
    public List<ConfigurationFile> imports() {
        return imports;
    }
}