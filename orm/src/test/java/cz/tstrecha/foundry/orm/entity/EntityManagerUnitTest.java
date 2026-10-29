package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityManagerUnitTest {

    private EntityManager newEntityManager() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var registry = new PersisterRegistry(new DescriptorRegistry(List.of(EntitiesMarker.class)));
        return new EntityManager(new ConnectionProvider(ds), registry);
    }

    @Test
    void findRejectsNullType() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.find(null, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void findRejectsNullId() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.find(BookEntity.class, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void findRejectsUnregisteredType() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.find(String.class, "x"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void findAllRejectsNullType() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.findAll(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void findAllRejectsUnregisteredType() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.findAll(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void persistRejectsNullEntity() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.persist(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void persistRejectsUnregisteredType() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.persist("hello"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void runInTransactionRejectsNullRunnable() throws Exception {
        try (var em = newEntityManager()) {
            assertThatThrownBy(() -> em.runInTransaction(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void runInTransactionRollbackFailurePreservesOriginalException() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        doThrow(new SQLException("rollback failed")).when(conn).rollback();
        var registry = new PersisterRegistry(new DescriptorRegistry(List.of(EntitiesMarker.class)));
        try (var em = new EntityManager(new ConnectionProvider(ds), registry)) {
            var original = new RuntimeException("original");
            assertThatThrownBy(() -> em.runInTransaction(tx -> { throw original; }))
                    .satisfiesAnyOf(
                            ex -> assertThat(ex).hasRootCauseMessage("original"),
                            ex -> assertThat(ex).hasCauseInstanceOf(RuntimeException.class)
                                    .cause().hasMessageContaining("original"));
        }
    }

    @Test
    void nullProviderInConstructorIsRejected() throws Exception {
        var registry = new PersisterRegistry(new DescriptorRegistry(List.of(EntitiesMarker.class)));
        assertThatThrownBy(() -> new EntityManager(null, registry))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullRegistryInConstructorIsRejected() {
        var ds = mock(DataSource.class);
        assertThatThrownBy(() -> new EntityManager(new ConnectionProvider(ds), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void runInTransactionPreservesOriginalExceptionWhenRollbackThrowsIllegalState() throws Exception {
        try (var em = newEntityManager()) {
            var original = new RuntimeException("business-error");
            assertThatThrownBy(() -> em.runInTransaction(tx -> {
                tx.rollback();           // leave the transaction inactive
                throw original;          // triggers the catch block, which will try to rollback again
            })).satisfiesAnyOf(
                    ex -> assertThat(ex).hasRootCauseMessage("business-error"),
                    ex -> assertThat(ex).cause().hasMessageContaining("business-error"),
                    ex -> assertThat(ex.getSuppressed()).anyMatch(s -> s.getMessage().contains("business-error")));
        }
    }

    @Test
    void flushWithoutDirtyEntitiesIsNoop() throws Exception {
        try (var em = newEntityManager()) {
            em.flush();
        }
    }
}
