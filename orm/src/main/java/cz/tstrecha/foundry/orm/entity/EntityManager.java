package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.transaction.Transaction;
import cz.tstrecha.foundry.orm.entity.context.EntityPersistenceBag;
import cz.tstrecha.foundry.orm.entity.context.PersistenceContext;
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
    private final PersistenceContext persistenceContext;

    public EntityManager(ConnectionProvider connectionProvider, PersisterRegistry persisterRegistry) {
        this.session = new DBSession(connectionProvider);
        this.persisterRegistry = persisterRegistry;
        this.persistenceContext = new PersistenceContext();
    }

    public <T, ID> Optional<T> find(Class<T> entityType, ID id) {
        var cached = persistenceContext.getByKey(entityType, id);
        if(cached.isPresent()) {
            return cached.map(EntityPersistenceBag::getEntity);
        }

        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelectById(id);

        var entity = session.selectOne(sql, entityPersister.createRowMapperBuilder());
        entity.ifPresent(e -> persistenceContext.makeEntityHandled(entityPersister, e));

        return entity;
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelect();

        var entities = session.selectAll(sql, entityPersister.createRowMapperBuilder());
        entities.forEach(entity -> persistenceContext.makeEntityHandled(entityPersister, entity));

        return entities;
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

    public void flush() {
        persistenceContext.checkForDirtyEntities().forEach(this::flushEntity);
    }

    public <T> void flushEntity(PersistenceContext.DirtyEntityBag<T> dirtyEntityBag) {
        var key = dirtyEntityBag.entityKey();
        var entityPersister = persisterRegistry.getPersister(key.entityType());

        var sql = entityPersister.generateUpdate(key.key(), dirtyEntityBag.entity(), dirtyEntityBag.dirtyColumns());
        var rowsCreated = session.update(sql);
        if(rowsCreated != 1) {
            throw new IllegalStateException("Database created 0 new rows.");
        }
    }

    public void runInTransaction(TransactionRunnable runnable) {
        var tx = getTransaction();
        try {
            tx.begin();
            runnable.run(tx);
            flush();
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
    public interface TransactionRunnable {

        void run(Transaction tx) throws Exception;

    }
}
