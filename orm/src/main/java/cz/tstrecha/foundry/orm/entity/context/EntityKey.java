package cz.tstrecha.foundry.orm.entity.context;

public record EntityKey<T>(Class<T> entityType, Object key) {

}
