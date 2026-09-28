package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.entity.system.SystemEntities;
import lombok.Getter;

import java.util.Map;

public class EntityContext {

    private final Map<Class<?>, EntityDefinition<?>> entityDefinitions;
    private final Map<Class<?>, EntityCreationStrategy<?>> entityCreationStrategies;

    public EntityContext(Class<?> entitySourceRoot) {
        entityDefinitions = EntityScanner.scan(SystemEntities.class, entitySourceRoot);
        entityCreationStrategies = EntityScanner.scanStrategies(SystemEntities.class, entitySourceRoot);
    }

    public <T> EntityDefinition<T> getEntityDefinition(Class<T> clazz) {
        return (EntityDefinition<T>) entityDefinitions.get(clazz);
    }

    public <T> EntityCreationStrategy<T> getEntityCreationStrategy(Class<T> clazz) {
        return (EntityCreationStrategy<T>) entityCreationStrategies.get(clazz);
    }

}
