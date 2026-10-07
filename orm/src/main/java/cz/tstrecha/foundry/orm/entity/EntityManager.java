package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.transaction.Transaction;
import cz.tstrecha.foundry.orm.entity.registry.PersisterRegistry;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EntityManager implements Closeable {

    private final DBSession session;
    private final PersisterRegistry persisterRegistry;

    public EntityManager(ConnectionProvider connectionProvider, PersisterRegistry persisterRegistry) {
        this.session = new DBSession(connectionProvider);
        this.persisterRegistry = persisterRegistry;
    }

    public <T, ID> Optional<T> find(Class<T> entityType, ID id) {
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelectById(id);

        return session.selectOne(sql, entityPersister.createRowMapperBuilder());
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelect();

        return session.selectAll(sql, entityPersister.createRowMapperBuilder());
    }

    public void persist(Object entity) {
        var entityType = entity.getClass();
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateInsert(entityPersister.getManagedType().getType().cast(entity));

        var rowsCreated = session.insert(sql);
        if(rowsCreated != 1) {
            throw new IllegalStateException("Database created 0 new rows.");
        }
    }

    public void runInTransaction(RunnableWithException runnable) {
        var tx = getTransaction();
        try {
            tx.begin();
            runnable.run();
            tx.commit();
        } catch (Exception ex) {
            try {
                tx.rollback();
                throw new RuntimeException(ex);
            } catch (SQLException rollbackEx) {
                throw new RuntimeException(rollbackEx);
            }
        }
    }

    public Transaction getTransaction() {
        return this.session.getTransaction();
    }

    @Override
    public void close() {
        this.session.close();
    }

    @FunctionalInterface
    public interface RunnableWithException {

        void run() throws Exception;

    }
}
