package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.uuidgen.UuidGenMarker;
import cz.tstrecha.foundry.orm.fixtures.uuidgen.UuidWidgetEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavior-first contract for the UUID id generator.
 *
 * <p>These tests describe what persist(entity) must do when the entity carries
 * {@code @GeneratedValue(strategy = UUID)}. They avoid naming any internal
 * production type (no UuidGenerator, no strategy enum) so the implementation
 * is free to evolve — the contract is just: "after persist, the entity has a
 * non-null UUID and that UUID identifies the row in the database".
 */
class UuidGeneratorIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setupSchema() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS uuid_widget (
                    id UUID PRIMARY KEY,
                    label TEXT
                );
                """);
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(UuidGenMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        execute("TRUNCATE uuid_widget;");
    }

    private UuidWidgetEntity widget(String label) {
        var w = new UuidWidgetEntity();
        w.setLabel(label);
        return w;
    }

    @Test
    void persistWithNullIdPopulatesUuidOnTheEntityInstance() throws Exception {
        var w = widget("alpha");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        assertThat(w.getId())
                .as("after persist, the id must be visible on the caller's instance — the whole point of the UUID strategy is that the caller does not supply the id, but the entity is still usable afterwards")
                .isNotNull();
    }

    @Test
    void persistTwoEntitiesWithNullIdAssignsDistinctUuids() throws Exception {
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
                .as("two persists must not share an id — a UUID generator that hands the same value twice would corrupt the primary key and defeats the point of UUIDs")
                .isNotEqualTo(b.getId());
    }

    @Test
    void persistedEntityIsFindableByItsGeneratedUuid() throws Exception {
        var w = widget("findable");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        try (var em = context.openSession()) {
            var found = em.find(UuidWidgetEntity.class, w.getId()).orElseThrow();
            assertThat(found.getLabel())
                    .as("the generated UUID must identify the row that was just written — otherwise insert and lookup are using different values")
                    .isEqualTo("findable");
        }
    }

    @Test
    void persistWithCallerSuppliedUuidKeepsThatUuid() throws Exception {
        var fixed = UUID.fromString("00000000-0000-0000-0000-000000000042");
        var w = widget("preassigned");
        w.setId(fixed);

        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }

        assertThat(w.getId())
                .as("JPA contract: if the caller supplies an id, the generator must not overwrite it — otherwise explicit control is impossible")
                .isEqualTo(fixed);

        try (var em = context.openSession()) {
            var found = em.find(UuidWidgetEntity.class, fixed).orElseThrow();
            assertThat(found.getLabel())
                    .as("the caller-supplied id must also be what the row ends up stored under in the database")
                    .isEqualTo("preassigned");
        }
    }

    @Test
    void persistingManyEntitiesInOneTransactionAssignsOnlyUniqueUuids() throws Exception {
        var widgets = Stream.of("1", "2", "3", "4", "5", "6", "7", "8").map(this::widget).toList();
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> widgets.forEach(em::persist));
        }

        var ids = widgets.stream().map(UuidWidgetEntity::getId).toList();
        assertThat(ids)
                .as("every persist must get its own fresh UUID — none null, none duplicated — even inside the same transaction")
                .doesNotContainNull();
        assertThat(new HashSet<>(ids))
                .as("duplicates among the generated UUIDs would mean the generator is not actually random — a correctness disaster for every caller relying on UUID uniqueness")
                .hasSameSizeAs(ids);
    }

    @Test
    void generatedUuidRoundTripsIntactThroughTheDatabase() throws Exception {
        var w = widget("roundtrip");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(w));
        }
        var generated = w.getId();

        try (var em = context.openSession()) {
            var found = em.find(UuidWidgetEntity.class, generated).orElseThrow();
            assertThat(found.getId())
                    .as("the UUID stored in the row must equal the one that was generated into the entity — otherwise the id used for insert differs from the one in the database")
                    .isEqualTo(generated);
        }
    }
}
