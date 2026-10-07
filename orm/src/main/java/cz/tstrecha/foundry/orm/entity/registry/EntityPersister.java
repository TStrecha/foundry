package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.connection.sql.FilterBuilder;
import cz.tstrecha.foundry.orm.connection.sql.InsertSql;
import cz.tstrecha.foundry.orm.connection.sql.SelectSql;
import cz.tstrecha.foundry.orm.connection.sql.SqlOperator;
import cz.tstrecha.foundry.orm.entity.context.EntityKey;
import cz.tstrecha.foundry.orm.entity.row.RowMapperBuilder;
import lombok.Getter;
import lombok.SneakyThrows;

import static cz.tstrecha.foundry.orm.connection.sql.Sql.insert;
import static cz.tstrecha.foundry.orm.connection.sql.Sql.select;

public class EntityPersister<T> {

    @Getter
    private final ManagedType<T> managedType;
    private final Class<T> type;

    public EntityPersister(ManagedType<T> managedType) {
        this.managedType = managedType;
        this.type = managedType.getType();
    }

    public SelectSql generateSelectById(Object id) {
        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName())
                .where(new FilterBuilder(managedType.getIdColumn().getColumnLabel(), SqlOperator.EQUALS, id));
    }

    @SneakyThrows
    public InsertSql generateInsert(Object entity) {
        var typedEntity = type.cast(entity);
        var values = managedType.getValueBindersForEntity(typedEntity);

        return insert()
                .into(managedType.getTableName(), managedType.getManagedColumnLabels())
                .values(values);
    }

    public SelectSql generateSelect() {
        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName());
    }

    public EntityKey buildEntityKey(T entity) {
        var key = managedType.extractKeyFromEntity(entity);
        return new EntityKey(type, key);
    }

    @SneakyThrows
    public RowMapperBuilder<T> createRowMapperBuilder() {
        return this.managedType::createRowMapper;
    }

}
