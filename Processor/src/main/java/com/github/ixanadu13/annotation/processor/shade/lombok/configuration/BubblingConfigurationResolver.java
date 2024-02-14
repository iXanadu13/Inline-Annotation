package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

public class BubblingConfigurationResolver implements ConfigurationResolver {

    private final ConfigurationFile start;
    private final ConfigurationFileToSource fileMapper;

    public BubblingConfigurationResolver(ConfigurationFile start, ConfigurationFileToSource fileMapper) {
        this.start = start;
        this.fileMapper = fileMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(ConfigurationKey<T> key) {
        boolean isList = key.getType().isList();
        List<List<ConfigurationSource.ListModification>> listModificationsList = null;

        boolean stopBubbling = false;
        ConfigurationFile currentLevel = start;
        Collection<ConfigurationFile> visited = new HashSet<ConfigurationFile>();
        outer:
        while (currentLevel != null) {
            Deque<ConfigurationFile> round = new ArrayDeque<ConfigurationFile>();
            round.push(currentLevel);

            while (!round.isEmpty()) {
                ConfigurationFile currentFile = round.pop();
                if (currentFile == null || !visited.add(currentFile)) continue;

                ConfigurationSource source = fileMapper.parsed(currentFile);
                if (source == null) continue;

                for (ConfigurationFile importFile : source.imports()) round.push(importFile);

                ConfigurationSource.Result stop = source.resolve(ConfigurationKeys.STOP_BUBBLING);
                stopBubbling = stopBubbling || (stop != null && Boolean.TRUE.equals(stop.getValue()));

                ConfigurationSource.Result result = source.resolve(key);
                if (result == null) continue;

                if (isList) {
                    if (listModificationsList == null) listModificationsList = new ArrayList<List<ConfigurationSource.ListModification>>();
                    listModificationsList.add((List<ConfigurationSource.ListModification>) result.getValue());
                }
                if (result.isAuthoritative()) {
                    if (isList) break outer;
                    return (T) result.getValue();
                }
            }
            if (stopBubbling) break;
            currentLevel = currentLevel.parent();
        }

        if (!isList) return null;
        if (listModificationsList == null) return (T) Collections.emptyList();

        List<Object> listValues = new ArrayList<Object>();
        Collections.reverse(listModificationsList);
        for (List<ConfigurationSource.ListModification> listModifications : listModificationsList) {
            if (listModifications != null) for (ConfigurationSource.ListModification modification : listModifications) {
                listValues.remove(modification.getValue());
                if (modification.isAdded()) listValues.add(modification.getValue());
            }
        }
        return (T) listValues;
    }
}
