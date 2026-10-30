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

class CrudIntegrationTest extends AbstractPostgresIntegrationTest {

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

    private BookEntity book(Long id, String title, Genre genre, Long pages) {
        var b = new BookEntity();
        b.setId(id);
        b.setTitle(title);
        b.setAuthor("A");
        b.setGenre(genre);
        b.setPageCount(pages);
        return b;
    }

    @Test
    void persistInsertsRowAndFindReturnsIt() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(1L, "Dune", Genre.FICTION, 500L));
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            var found = em.find(BookEntity.class, 1L);
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Dune");
            assertThat(found.get().getGenre()).isEqualTo(Genre.FICTION);
            assertThat(found.get().getPageCount()).isEqualTo(500L);
        }
    }

    @Test
    void findReturnsEmptyWhenRowMissing() throws Exception {
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 999L)).isEmpty();
        }
    }

    @Test
    void findAllReturnsAllRowsAndRegistersThemInContext() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(1L, "A", Genre.FICTION, 1L));
            em.persist(book(2L, "B", Genre.POETRY, 2L));
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            var all = em.findAll(BookEntity.class);
            assertThat(all).hasSize(2);
            assertThat(all).extracting(BookEntity::getId).containsExactlyInAnyOrder(1L, 2L);
        }
    }

    @Test
    void findSameIdTwiceReturnsSameInstanceFromPersistenceContext() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(1L, "cached", Genre.POETRY, 1L));
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            var first = em.find(BookEntity.class, 1L).orElseThrow();
            var second = em.find(BookEntity.class, 1L).orElseThrow();
            assertThat(first).isSameAs(second);
        }
    }

    @Test
    void persistWithDuplicateIdThrows() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            em.persist(book(1L, "A", Genre.FICTION, 1L));
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            assertThatThrownBy(() -> em.persist(book(1L, "A", Genre.FICTION, 1L)))
                    .isInstanceOf(Exception.class);
            em.getTransaction().rollback();
        }
    }

    @Test
    void findAllOnEmptyTableReturnsEmptyList() throws Exception {
        try (var em = context.openSession()) {
            assertThat(em.findAll(BookEntity.class)).isEmpty();
        }
    }

    @Test
    void persistEntityWithNullStringColumnWorks() throws Exception {
        try (var em = context.openSession()) {
            em.getTransaction().begin();
            var b = new BookEntity();
            b.setId(10L);
            b.setGenre(Genre.POETRY);
            b.setPageCount(1L);
            em.persist(b);
            em.getTransaction().commit();
        }
        try (var em = context.openSession()) {
            var found = em.find(BookEntity.class, 10L).orElseThrow();
            assertThat(found.getTitle()).isNull();
        }
    }

    @Test
    void findRowWithNullEnumColumnReturnsEntityWithNullField() throws Exception {
        execute("INSERT INTO book(id, title, author, genre, page_count) " +
                "VALUES (99, 't', 'a', NULL, 1);");
        try (var em = context.openSession()) {
            var row = em.find(BookEntity.class, 99L).orElseThrow();
            assertThat(row.getGenre()).isNull();
        }
    }

    @Test
    void findRowWithNullLongColumnReturnsEntityWithNullField() throws Exception {
        execute("INSERT INTO book(id, title, author, genre, page_count) " +
                "VALUES (100, 't', 'a', 'FICTION', NULL);");
        try (var em = context.openSession()) {
            var row = em.find(BookEntity.class, 100L).orElseThrow();
            assertThat(row.getPageCount()).isNull();
        }
    }

    @Test
    void persistWithoutTransactionAutoCommits() throws Exception {
        try (var em = context.openSession()) {
            em.persist(book(42L, "no-tx", Genre.POETRY, 1L));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 42L)).isPresent();
        }
    }

    @Test
    void unicodeTitleRoundtripsIntact() throws Exception {
        var payload = "Příběh — 中文 — 😀 — a O'Brian";
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(50L, payload, Genre.POETRY, 1L)));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 50L).orElseThrow().getTitle())
                    .isEqualTo(payload);
        }
    }

    @Test
    void stringWithSingleQuoteIsNotInjected() throws Exception {
        var payload = "'; DROP TABLE book; --";
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(51L, payload, Genre.FICTION, 1L)));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 51L).orElseThrow().getTitle())
                    .isEqualTo(payload);
            assertThat(em.findAll(BookEntity.class)).isNotEmpty();
        }
    }

    @Test
    void negativeIdValueIsSupported() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(-100L, "neg", Genre.FICTION, 1L)));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, -100L)).isPresent();
        }
    }

    @Test
    void extremeLongPageCountRoundtripsIntact() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(200L, "max", Genre.FICTION, Long.MAX_VALUE)));
            em.runInTransaction(tx -> em.persist(book(201L, "min", Genre.FICTION, Long.MIN_VALUE)));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(BookEntity.class, 200L).orElseThrow().getPageCount())
                    .isEqualTo(Long.MAX_VALUE);
            assertThat(em.find(BookEntity.class, 201L).orElseThrow().getPageCount())
                    .isEqualTo(Long.MIN_VALUE);
        }
    }

    @Test
    void persistenceContextIsIsolatedPerSession() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(300L, "iso", Genre.FICTION, 1L)));
        }
        try (var em1 = context.openSession(); var em2 = context.openSession()) {
            var a = em1.find(BookEntity.class, 300L).orElseThrow();
            var b = em2.find(BookEntity.class, 300L).orElseThrow();
            assertThat(a).isNotSameAs(b);
        }
    }

    @Test
    void sameFindAcrossSessionsReturnsIndependentEntities() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(book(301L, "orig", Genre.FICTION, 1L)));
        }
        try (var s1 = context.openSession()) {
            s1.getTransaction().begin();
            s1.find(BookEntity.class, 301L).orElseThrow().setTitle("session1-change");
            try (var s2 = context.openSession()) {
                assertThat(s2.find(BookEntity.class, 301L).orElseThrow().getTitle())
                        .isEqualTo("orig");
            }
            s1.getTransaction().rollback();
        }
    }
}
