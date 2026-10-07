package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.system.SystemEntities;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PersisterRegistry {

    private final Map<Class<?>, EntityPersister<?>> persisters = new HashMap<>();

    public PersisterRegistry(List<Class<?>> scanningRoots) throws NoSuchMethodException {
        var packageNames = Stream.concat(scanningRoots.stream(), Stream.of(SystemEntities.class))
                .map(Class::getPackageName)
                .toArray(String[]::new);
        var reflectionsConfig = new ConfigurationBuilder().forPackages(packageNames);
        var reflections = new Reflections(reflectionsConfig);

        var entities = reflections.getTypesAnnotatedWith(Table.class);
        for (var entity : entities) {
            var managedType = new ManagedType<>(entity);
            var persister = new EntityPersister<>(managedType);
            persisters.put(entity, persister);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> EntityPersister<T> getPersister(Class<T> type) {
        return (EntityPersister<T>) persisters.get(type);
    }
}
