package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.connection.sql.FilterBuilder;
import cz.tstrecha.foundry.orm.connection.sql.SelectSql;
import cz.tstrecha.foundry.orm.connection.sql.SqlOperator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;

import static cz.tstrecha.foundry.orm.connection.sql.Sql.select;

public class EntityPersister<T> {

    @Getter
    private final ManagedType<T> managedType;

    public EntityPersister(ManagedType<T> managedType) {
        this.managedType = managedType;
    }

    public SelectSql generateSelectById(Object id) {
        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName())
                .where(new FilterBuilder(managedType.getIdColumn().getColumnLabel(), SqlOperator.EQUALS, id));
    }

    public SelectSql generateSelect() {
        return select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName());
    }

    @SneakyThrows
    public T createAndSaturateInstanceOf(List<String> columnLabels, List<String> row) {
        return this.managedType.createAndSaturateInstance(columnLabels, row);
    }

}
