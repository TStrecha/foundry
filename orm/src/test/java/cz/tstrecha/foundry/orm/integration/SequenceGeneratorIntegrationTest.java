package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.generated.GeneratedMarker;
import cz.tstrecha.foundry.orm.fixtures.generated.SequencedWidgetEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavior-first contract for the SEQUENCE id generator.
 *
 * <p>These tests describe what persist(entity) must do when the entity carries
 * {@code @GeneratedValue(strategy = SEQUENCE)} and arrives with id == null.
 * They do not refer to any internal interface (no IdGenerator, no generator
 * strategy enum) so the production design is free to evolve.
 */
class SequenceGeneratorIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setupSchema() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS widget (
                    id BIGINT PRIMARY KEY,
                    label TEXT
                );
                """);
        execute("CREATE SEQUENCE IF NOT EXISTS widget_seq START WITH 1 INCREMENT BY 1;");
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(GeneratedMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        execute("TRUNCATE widget;");
        execute("ALTER SEQUENCE widget_seq RESTART WITH 1;");
    }

    private SequencedWidgetEntity widget(String label) {
        var w = new SequencedWidgetEntity();
        w.setLabel(label);
        return w;
    }

    @Test
    void persistWithNullIdPopulatesIdOnTheEntityInstance() throws Exception {
        var w = widget("alpha");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        assertThat(w.getId())
                .as("after persist, the id must be visible on the caller's instance — the whole point of a generator is that the caller does not need to supply it, but the entity is still usable afterward")
                .isNotNull();
    }

    @Test
    void persistTwoEntitiesWithNullIdAssignsDistinctIds() throws Exception {
        var a = widget("A");
        var b = widget("B");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                em.persist(a);
                em.persist(b);
            });
        }

        assertThat(a.getId()).isNotNull();
        assertThat(b.getId()).isNotNull();
        assertThat(a.getId())
                .as("two persists must not share an id — a generator that hands the same value twice would corrupt the primary key")
                .isNotEqualTo(b.getId());
    }

    @Test
    void persistedEntityIsFindableByItsGeneratedId() throws Exception {
        var w = widget("findable");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        try (var em = context.openSession()) {
            var found = em.find(SequencedWidgetEntity.class, w.getId()).orElseThrow();
            assertThat(found.getLabel())
                    .as("the generated id must identify the row that was just written — otherwise insert and lookup are using different values")
                    .isEqualTo("findable");
        }
    }

    @Test
    void generatedIdsComeFromTheConfiguredSequence() throws Exception {
        var w = widget("first");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        assertThat(w.getId())
                .as("widget_seq starts at 1 with increment 1 (and the suite truncates before each test), so the first assigned id must be 1 — proves the ORM is actually calling the configured sequence, not a counter it made up")
                .isEqualTo(1L);
    }

    @Test
    void persistingManyEntitiesInOneTransactionAssignsStrictlyIncreasingIds() throws Exception {
        var ws = List.of(widget("1"), widget("2"), widget("3"), widget("4"));
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> ws.forEach(em::persist));
        }

        var ids = ws.stream().map(SequencedWidgetEntity::getId).toList();
        assertThat(ids).doesNotContainNull();
        assertThat(ids)
                .as("a monotonically increasing sequence must yield monotonically increasing ids within a single transaction — otherwise ordering invariants callers rely on break")
                .isSorted();
    }
}
