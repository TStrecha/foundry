package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.persister.ManagedColumn;
import cz.tstrecha.foundry.orm.entity.persister.ManagedType;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@Getter
public class EntityPersistenceBag<T> {

    private final EntityKey<T> key;
    private final T entity;
    private final ManagedType<T> managedType;
    private Map<ManagedColumn<T, ?>, ?> snapshot;

    public EntityPersistenceBag(EntityKey<T> key, T entity, ManagedType<T> managedType) {
        requireNotNullArgument(key, "key");
        requireNotNullArgument(entity, "entity");
        requireNotNullArgument(managedType, "managedType");

        this.key = key;
        this.entity = entity;
        this.managedType = managedType;
        this.snapshot = managedType.createEntitySnapshot(entity);
    }

    @Nullable
    public DirtyEntityBag<T> createDirtyEntityBagIfDirty() {
        var current = managedType.createEntitySnapshot(entity);
        var dirtyColumns = new ArrayList<ManagedColumn<T, ?>>();

        for(var column : snapshot.keySet()) {
            var snapshotValue = snapshot.get(column);
            var currentValue = current.get(column);

            if(!Objects.equals(snapshotValue, currentValue)) {
                dirtyColumns.add(column);
            }
        }

        if(dirtyColumns.isEmpty()) {
            return null;
        }

        this.snapshot = current;

        return new DirtyEntityBag<>(getKey(), getEntity(), dirtyColumns);
    }
}
