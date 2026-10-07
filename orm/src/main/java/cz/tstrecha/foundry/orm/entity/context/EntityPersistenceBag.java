package cz.tstrecha.foundry.orm.entity.context;

import lombok.Getter;

@Getter
public class EntityPersistenceBag<T> {

    private final EntityKey key;
    private final T entity;

    public EntityPersistenceBag(EntityKey key, T entity) {
        this.key = key;
        this.entity = entity;
    }
}
