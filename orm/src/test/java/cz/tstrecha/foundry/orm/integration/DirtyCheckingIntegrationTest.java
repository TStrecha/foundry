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

class DirtyCheckingIntegrationTest extends AbstractPostgresIntegrationTest {

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

    private BookEntity seed(long id) throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            var b = new BookEntity();
            b.setId(id);
            b.setTitle("orig");
            b.setAuthor("orig");
            b.setGenre(Genre.FICTION);
            b.setPageCount(10L);
            em.persist(b);
            em.getTransaction().commit();
            return b;
        }
    }

    @Test
    void mutatingLoadedEntityAndFlushingPersistsChange() throws Exception {
        seed(1L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var book = em.find(BookEntity.class, 1L).orElseThrow();
                book.setTitle("updated");
            });
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 1L).orElseThrow().getTitle())
                    .isEqualTo("updated");
        }
    }

    @Test
    void mutatingMultipleColumnsEmitsSingleUpdate() throws Exception {
        seed(2L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var book = em.find(BookEntity.class, 2L).orElseThrow();
                book.setTitle("new title");
                book.setAuthor("new author");
                book.setPageCount(99L);
            });
        }
        try (var em = context.openSession()) {
            var reloaded = em.find(BookEntity.class, 2L).orElseThrow();
            assertThat(reloaded.getTitle()).isEqualTo("new title");
            assertThat(reloaded.getAuthor()).isEqualTo("new author");
            assertThat(reloaded.getPageCount()).isEqualTo(99L);
        }
    }

    @Test
    void persistedEntityIsTrackedForDirtyChecking() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var b = new BookEntity();
                b.setId(99L);
                b.setTitle("initial");
                b.setAuthor("a");
                b.setGenre(Genre.FICTION);
                b.setPageCount(1L);
                em.persist(b);
                b.setTitle("after persist mutation");
            });
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 99L).orElseThrow().getTitle())
                    .isEqualTo("after persist mutation");
        }
    }

    @Test
    void flushingTwiceWithoutMutationIsNoop() throws Exception {
        seed(3L);
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            var book = em.find(BookEntity.class, 3L).orElseThrow();
            book.setTitle("mutated");
            em.flush();
            em.flush();
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 3L).orElseThrow().getTitle())
                    .isEqualTo("mutated");
        }
    }

    @Test
    void updateFailsWhenRowDeletedUnderUs() throws Exception {
        seed(4L);
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            var book = em.find(BookEntity.class, 4L).orElseThrow();
            execute("DELETE FROM book WHERE id = 4;");
            book.setTitle("x");
            assertThatThrownBy(em::flush)
                    .isInstanceOf(IllegalStateException.class);
            em.getTransaction().rollback();
        }
    }

    @Test
    void transientMutationIsClean() throws Exception {
        seed(5L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var book = em.find(BookEntity.class, 5L).orElseThrow();
                var original = book.getTitle();
                book.setTitle("temporary");
                book.setTitle(original);
            });
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 5L).orElseThrow().getTitle())
                    .isEqualTo("orig");
        }
    }

    @Test
    void mutatingLongColumnBackToNullIsPersisted() throws Exception {
        seed(6L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var book = em.find(BookEntity.class, 6L).orElseThrow();
                book.setPageCount(null);
            });
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 6L).orElseThrow().getPageCount()).isNull();
        }
    }

    @Test
    void mutatingEnumToNullIsPersisted() throws Exception {
        seed(7L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                var book = em.find(BookEntity.class, 7L).orElseThrow();
                book.setGenre(null);
            });
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 7L).orElseThrow().getGenre()).isNull();
        }
    }
}
