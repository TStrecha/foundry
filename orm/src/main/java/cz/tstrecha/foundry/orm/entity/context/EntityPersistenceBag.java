package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.registry.ManagedColumn;
import cz.tstrecha.foundry.orm.entity.registry.ManagedType;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class EntityPersistenceBag<T> {

    private final EntityKey<T> key;
    private final T entity;
    private final ManagedType<T> managedType;
    private final List<?> snapshot;

    public EntityPersistenceBag(EntityKey<T> key, T entity, ManagedType<T> managedType) {
        this.key = key;
        this.entity = entity;
        this.managedType = managedType;
        this.snapshot = Collections.unmodifiableList(managedType.createEntitySnapshot(entity));
    }

    @Nullable
    public PersistenceContext.DirtyEntityBag<T> createDirtyEntityBagIfDirty() {
        var current = managedType.createEntitySnapshot(entity);
        var dirtyColumns = new ArrayList<ManagedColumn<T, ?>>();

        for(var i = 0; i < snapshot.size(); i++) {
            var snapshotValue = snapshot.get(i);
            var currentValue = current.get(i);

            if(!Objects.equals(snapshotValue, currentValue)) {
                var column = managedType.getColumnByIndex(i);
                dirtyColumns.add(column);
            }
        }

        if(dirtyColumns.isEmpty()) {
            return null;
        }

        return new PersistenceContext.DirtyEntityBag<>(getKey(), getEntity(), dirtyColumns);
    }
}
