package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.registry.ManagedColumn;

import java.util.List;

public record DirtyEntityBag<T>(EntityKey<T> entityKey, T entity, List<ManagedColumn<T, ?>> dirtyColumns) {

}