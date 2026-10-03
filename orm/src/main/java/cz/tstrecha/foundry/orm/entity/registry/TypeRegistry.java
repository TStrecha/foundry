package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.system.SystemEntities;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TypeRegistry {

    private final Map<Class<?>, ManagedType<?>> registry = new HashMap<>();

    public TypeRegistry(List<Class<?>> scanningRoots) throws NoSuchMethodException {
        var packageNames = Stream.concat(scanningRoots.stream(), Stream.of(SystemEntities.class))
                .map(Class::getPackageName)
                .toArray(String[]::new);
        var reflectionsConfig = new ConfigurationBuilder().forPackages(packageNames);
        var reflections = new Reflections(reflectionsConfig);

        var entities = reflections.getTypesAnnotatedWith(Table.class);
        for (var entity : entities) {
            registry.put(entity, new ManagedType<>(entity));
        }
    }

    public <T> ManagedType<T> getManagedType(Class<T> type) {
        //todo casting
        return (ManagedType<T>) registry.get(type);
    }

    @SneakyThrows//todo
    public <T> T createAndSaturateInstanceOf(Class<T> type, List<String> columnLabels, List<String> row) {
        //todo casting
        return (T) registry.get(type).createAndSaturateInstance(columnLabels, row);
    }
}
