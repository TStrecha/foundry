package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.registry.EntityPersister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersistenceContext {

    private Map<EntityKey, EntityPersistenceBag<?>> context = new HashMap<>();

    public <T> Optional<EntityPersistenceBag<T>> getByKey(EntityKey entityKey) {
        return Optional.ofNullable((EntityPersistenceBag<T>) this.context.get(entityKey));
    }

    public <T> Optional<EntityPersistenceBag<T>> getByKey(Class<T> type, Object key) {
        return Optional.ofNullable((EntityPersistenceBag<T>) this.context.get(new EntityKey(type, key)));
    }

    public <T> void makeEntityHandled(EntityPersister<T> persister, T entity) {
        var entityKey = persister.buildEntityKey(entity);
        context.put(entityKey, new EntityPersistenceBag<>(entityKey, entity));
    }

}
