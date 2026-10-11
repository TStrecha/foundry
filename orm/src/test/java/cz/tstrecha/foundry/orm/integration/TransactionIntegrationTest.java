package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setupSchema() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS book (
                    id BIGINT PRIMARY KEY,
                    title TEXT,
                    author TEXT,
                    genre TEXT,
                    page_count BIGINT
                );
                """);
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        execute("TRUNCATE book;");
    }

    private BookEntity book(long id) {
        var b = new BookEntity();
        b.setId(id);
        b.setTitle("t");
        b.setAuthor("a");
        b.setGenre(Genre.FICTION);
        b.setPageCount(1L);
        return b;
    }

    @Test
    void runInTransactionCommitsOnSuccess() {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(1L)));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 1L)).isPresent();
        }
    }

    @Test
    void runInTransactionRollsBackOnException() {
        try (var em = context.openSession()) {
            assertThatThrownBy(() -> em.runInTransaction(tx -> {
                em.persist(book(2L));
                throw new RuntimeException("boom");
            })).isInstanceOf(RuntimeException.class);
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 2L)).isEmpty();
        }
    }

    @Test
    void transactionCanBeManuallyBegunAndRolledBack() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(3L));
            em.getTransaction().rollback();
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 3L)).isEmpty();
        }
    }

    @Test
    void sessionCloseRollsBackOpenTransaction() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(4L));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 4L)).isEmpty();
        }
    }

    @Test
    void writesInDifferentSessionsAreIsolated() throws Exception {
        try (var writer = context.openSession()) {
            writer.getTransaction().begin();
            writer.persist(book(10L));
            try (var reader = context.openSession()) {
                assertThat(reader.find(BookEntity.class, 10L)).isEmpty();
            }
            writer.getTransaction().commit();
        }
        try (var reader = context.openSession()) {
            assertThat(reader.find(BookEntity.class, 10L)).isPresent();
        }
    }
}
