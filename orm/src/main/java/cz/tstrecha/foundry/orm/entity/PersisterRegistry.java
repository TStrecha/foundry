package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.persister.EntityPersister;
import cz.tstrecha.foundry.orm.entity.persister.ManagedType;

import java.util.HashMap;
import java.util.Map;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class PersisterRegistry {

    private final Map<Class<?>, EntityPersister<?>> persisters = new HashMap<>();

    public PersisterRegistry(DescriptorRegistry descriptorRegistry) throws NoSuchMethodException {
        requireNotNullArgument(descriptorRegistry, "descriptorRegistry");

        for (var descriptor : descriptorRegistry.getTypes().values()) {
            if(descriptor instanceof EntityDescriptor<?> entityDescriptor) {
                var managedType = new ManagedType<>(entityDescriptor);
                var persister = new EntityPersister<>(managedType);
                persisters.put(descriptor.type(), persister);
            }
        }
    }

    public boolean persisterExists(Class<?> entityType) {
        return persisters.containsKey(entityType);
    }

    @SuppressWarnings("unchecked")
    public <T> EntityPersister<T> getPersister(Class<T> type) {
        requireNotNullArgument(type, "type");

        return (EntityPersister<T>) persisters.get(type);
    }
}
