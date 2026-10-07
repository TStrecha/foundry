package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.sql.Sql;
import cz.tstrecha.foundry.orm.entity.registry.PersisterRegistry;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.util.List;

@RequiredArgsConstructor
public class EntityManager implements Closeable {

    private final DBSession session;
    private final PersisterRegistry persisterRegistry;

    public EntityManager(ConnectionProvider connectionProvider, PersisterRegistry persisterRegistry) {
        this.session = new DBSession(connectionProvider);
        this.persisterRegistry = persisterRegistry;
    }

    public <T, ID> T find(Class<T> entityType, ID id) {
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelectById(id);

        var result = session.selectOne(sql);

        return entityPersister.createAndSaturateInstanceOf(entityType, result.columnLabels(), result.row());
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelect();

        var result = session.selectAll(sql);

        return result.rows().stream()
                .map(row -> entityPersister.createAndSaturateInstanceOf(entityType, result.columnLabels(), row))
                .toList();
    }

    @Override
    public void close() {
        this.session.close();
    }
}
