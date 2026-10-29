package cz.tstrecha.foundry.orm.entity.persister;

import cz.tstrecha.foundry.orm.operation.filter.FilterBuilder;
import cz.tstrecha.foundry.orm.operation.filter.FilterClause;
import cz.tstrecha.foundry.orm.operation.sql.InsertSql;
import cz.tstrecha.foundry.orm.operation.sql.SelectSql;
import cz.tstrecha.foundry.orm.operation.filter.SqlOperator;
import cz.tstrecha.foundry.orm.operation.sql.UpdateSql;
import cz.tstrecha.foundry.orm.entity.context.EntityKey;
import cz.tstrecha.foundry.orm.entity.row.RowMapperBuilder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static cz.tstrecha.foundry.orm.operation.sql.Sql.insert;
import static cz.tstrecha.foundry.orm.operation.sql.Sql.select;
import static cz.tstrecha.foundry.orm.operation.sql.Sql.update;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotEmptyArgument;

public class EntityPersister<T> {

    @Getter
    private final ManagedType<T> managedType;
    private final Class<T> type;

    public EntityPersister(ManagedType<T> managedType) {
        requireNotNullArgument(managedType, "managedType");

        this.managedType = managedType;
        this.type = managedType.getType();
    }

    public SelectSql generateSelectById(Object id) {
        requireNotNullArgument(id, "id");

        var clauses = managedType.getIdColumns().stream()
                .map(idColumn -> new FilterClause(idColumn.getColumnLabel(), SqlOperator.EQUALS, idColumn.binderForAnyValue(id)))
                .toList();

        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName())
                .where(new FilterBuilder(clauses));
    }

    @SneakyThrows
    public InsertSql generateInsert(Object entity) {
        requireNotNullArgument(entity, "entity");

        var typedEntity = type.cast(entity);
        var values = managedType.getValueBindersForEntity(typedEntity);

        return insert()
                .into(managedType.getTableName(), managedType.getManagedColumnLabels())
                .values(values);
    }

    @SneakyThrows
    public UpdateSql generateUpdate(Object id, Object entity, List<ManagedColumn<T, ?>> columns) {
        requireNotNullArgument(id, "id");
        requireNotNullArgument(entity, "entity");
        requireNotEmptyArgument(columns, "fields");

        var typedEntity = type.cast(entity);
        var valueSetters = columns.stream()
                .collect(Collectors.toMap(
                        ManagedColumn::getColumnLabel,
                        column -> column.binderForEntity(typedEntity),
                        (x, _) -> x,
                        LinkedHashMap::new));

        var clauses = managedType.getIdColumns().stream()
                .map(idColumn -> new FilterClause(idColumn.getColumnLabel(), SqlOperator.EQUALS, idColumn.binderForAnyValue(id)))
                .toList();

        return update(managedType.getTableName())
                .set(valueSetters)
                .where(new FilterBuilder(clauses));
    }

    public SelectSql generateSelect() {
        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName());
    }

    public EntityKey<T> buildEntityKey(T entity) {
        requireNotNullArgument(entity, "entity");

        var key = managedType.extractKeyFromEntity(entity);
        return new EntityKey<>(type, key);
    }

    @SneakyThrows
    public RowMapperBuilder<T> createRowMapperBuilder() {
        return this.managedType::createRowMapper;
    }

}
