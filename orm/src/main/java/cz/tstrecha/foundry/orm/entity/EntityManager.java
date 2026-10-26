package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.transaction.Transaction;
import cz.tstrecha.foundry.orm.entity.context.DirtyEntityBag;
import cz.tstrecha.foundry.orm.entity.context.EntityPersistenceBag;
import cz.tstrecha.foundry.orm.entity.context.PersistenceContext;
import cz.tstrecha.foundry.orm.entity.registry.EntityPersister;
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
        if(connectionProvider == null) {
            throw new IllegalArgumentException("connectionProvider is null");
        }
        if(persisterRegistry == null) {
            throw new IllegalArgumentException("persisterRegistry is null");
        }

        this.session = new DBSession(connectionProvider);
        this.persisterRegistry = persisterRegistry;
        this.persistenceContext = new PersistenceContext();
    }

    public <T, ID> Optional<T> find(Class<T> entityType, ID id) {
        if(entityType == null) {
            throw new IllegalArgumentException("entityType is null");
        }
        if(!persisterRegistry.persisterExists(entityType)) {
            throw new IllegalArgumentException("No persister found for type " + entityType.getName());
        }

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
        if(entityType == null) {
            throw new IllegalArgumentException("entityType is null");
        }
        if(!persisterRegistry.persisterExists(entityType)) {
            throw new IllegalArgumentException("No persister found for type " + entityType.getName());
        }

        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateSelect();

        var entities = session.selectAll(sql, entityPersister.createRowMapperBuilder());
        entities.forEach(entity -> persistenceContext.makeEntityHandled(entityPersister, entity));

        return entities;
    }

    public <T> void persist(T entity) {
        if(entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        var entityType = entity.getClass();
        if(!persisterRegistry.persisterExists(entityType)) {
            throw new IllegalArgumentException("No persister found for type " + entityType.getName());
        }

        var entityPersister = persisterRegistry.getPersister(entityType);
        var sql = entityPersister.generateInsert(entity);

        var rowsCreated = session.insert(sql);
        if(rowsCreated != 1) {
            throw new IllegalStateException("Database created 0 new rows.");
        }

        persistenceContext.makeEntityHandled((EntityPersister<T>) entityPersister, entity);
    }

    public void flush() {
        persistenceContext.checkForDirtyEntities().forEach(this::flushEntity);
    }

    public <T> void flushEntity(DirtyEntityBag<T> dirtyEntityBag) {
        var key = dirtyEntityBag.entityKey();
        var entityPersister = persisterRegistry.getPersister(key.entityType());

        var sql = entityPersister.generateUpdate(key.key(), dirtyEntityBag.entity(), dirtyEntityBag.dirtyColumns());
        var rowsCreated = session.update(sql);
        if(rowsCreated != 1) {
            throw new IllegalStateException("Database updated 0 new rows.");
        }
    }

    public void runInTransaction(TransactionRunnable runnable) {
        if(runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }

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
            } catch (Exception rollbackEx) {
                throw new RuntimeException(ex);
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
