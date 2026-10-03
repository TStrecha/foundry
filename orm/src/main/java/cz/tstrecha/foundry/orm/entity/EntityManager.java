package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.sql.FilterBuilder;
import cz.tstrecha.foundry.orm.connection.sql.Sql;
import cz.tstrecha.foundry.orm.connection.sql.SqlOperator;
import cz.tstrecha.foundry.orm.entity.registry.TypeRegistry;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.util.List;

@RequiredArgsConstructor
public class EntityManager implements Closeable {

    private final DBSession session;
    private final TypeRegistry typeRegistry;

    public EntityManager(ConnectionProvider connectionProvider, TypeRegistry typeRegistry) {
        this.session = new DBSession(connectionProvider);
        this.typeRegistry = typeRegistry;
    }

    public <T, ID> T find(Class<T> entityType, ID id) {
        var managedType = typeRegistry.getManagedType(entityType);

        var sql = Sql
                .select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName())
                .where(new FilterBuilder(managedType.getIdColumn().getColumnLabel(), SqlOperator.EQUALS, id));

        var result = session.selectOne(sql);

        return typeRegistry.createAndSaturateInstanceOf(entityType, result.columnLabels(), result.row());
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var managedType = typeRegistry.getManagedType(entityType);

        var sql = Sql
                .select(managedType.getManagedColumnLabels())
                .from(managedType.getTableName());

        var result = session.selectAll(sql);

        return result.rows().stream()
                .map(row -> typeRegistry.createAndSaturateInstanceOf(entityType, result.columnLabels(), row))
                .toList();
    }

    @Override
    public void close() {
        this.session.close();
    }
}
