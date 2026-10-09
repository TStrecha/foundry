package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.registry.EntityPersister;
import cz.tstrecha.foundry.orm.entity.registry.ManagedColumn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PersistenceContext {

    private final Map<EntityKey<?>, EntityPersistenceBag<?>> context = new HashMap<>();

    public <T> Optional<EntityPersistenceBag<T>> getByKey(Class<T> type, Object key) {
        var entityKey = new EntityKey<>(type, key);
        var bag = (EntityPersistenceBag<T>) this.context.get(entityKey);
        return Optional.ofNullable(bag);
    }

    public <T> void makeEntityHandled(EntityPersister<T> persister, T entity) {
        var entityKey = persister.buildEntityKey(entity);
        context.put(entityKey, new EntityPersistenceBag<T>(entityKey, entity, persister.getManagedType()));
    }

    public List<DirtyEntityBag<?>> checkForDirtyEntities() {
        return context.values().stream()
                .<DirtyEntityBag<?>>map(EntityPersistenceBag::createDirtyEntityBagIfDirty)
                .filter(Objects::nonNull)
                .toList();
    }

}
