package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.EmbeddedMarker;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setupSchema() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS user_account (
                    id BIGINT PRIMARY KEY,
                    company_name TEXT,
                    first_name TEXT,
                    last_name TEXT,
                    title_before TEXT,
                    title_after TEXT
                );
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS composite_entity (
                    part_a TEXT NOT NULL,
                    part_b TEXT NOT NULL,
                    payload TEXT,
                    PRIMARY KEY (part_a, part_b)
                );
                """);
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(EmbeddedMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @BeforeEach
    void cleanTables() throws Exception {
        execute("TRUNCATE user_account;");
        execute("TRUNCATE composite_entity;");
    }

    // ------ @Embedded round-trip ------

    @Test
    void persistAndFindRoundTripsSingleLevelEmbeddedColumns() throws Exception {
        var user = new UserFixtureEntity(1L, "Acme", new NameFixture(null, "Alice", "Smith"));
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(user));
        }
        try (var em = context.openSession()) {
            var found = em.find(UserFixtureEntity.class, 1L).orElseThrow();
            assertThat(found.getCompanyName()).isEqualTo("Acme");
            assertThat(found.getName())
                    .as("one-level embedded must come back populated after a round-trip")
                    .isNotNull();
            assertThat(found.getName().getFirstName()).isEqualTo("Alice");
            assertThat(found.getName().getLastName()).isEqualTo("Smith");
        }
    }

    @Test
    void persistAndFindRoundTripsNestedEmbeddedColumns() throws Exception {
        var user = new UserFixtureEntity(2L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(user));
        }
        try (var em = context.openSession()) {
            var found = em.find(UserFixtureEntity.class, 2L).orElseThrow();
            assertThat(found.getName().getTitles())
                    .as("two-level nested embeddable must come back populated — not dropped by flatten/read")
                    .isNotNull();
            assertThat(found.getName().getTitles().getTitleBefore()).isEqualTo("Dr.");
            assertThat(found.getName().getTitles().getTitleAfter()).isEqualTo("PhD");
            assertThat(found.getName().getFirstName()).isEqualTo("Alice");
            assertThat(found.getName().getLastName()).isEqualTo("Smith");
        }
    }

    @Test
    void persistWithNullInnerEmbeddedWritesNullLeafColumnsAndRoundTrips() throws Exception {
        var user = new UserFixtureEntity(3L, "Acme", new NameFixture(null, "A", "B"));
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(user));
        }
        try (var em = context.openSession()) {
            var found = em.find(UserFixtureEntity.class, 3L).orElseThrow();
            assertThat(found.getName().getFirstName()).isEqualTo("A");
            assertThat(found.getName().getLastName()).isEqualTo("B");

            var titles = found.getName().getTitles();
            var allLeavesNull = titles == null
                    || (titles.getTitleBefore() == null && titles.getTitleAfter() == null);
            assertThat(allLeavesNull)
                    .as("inner embeddable left null on persist must come back either as null or as an instance whose leaves are all null — never as a partially-populated ghost")
                    .isTrue();
        }
    }

    @Test
    void persistWithTopLevelEmbeddedNullWritesAllLeafColumnsAsNull() throws Exception {
        var user = new UserFixtureEntity(4L, "Acme", null);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(user));
        }
        try (var em = context.openSession()) {
            var found = em.find(UserFixtureEntity.class, 4L).orElseThrow();

            var name = found.getName();
            var allLeavesNull = name == null
                    || (name.getFirstName() == null
                        && name.getLastName() == null
                        && (name.getTitles() == null
                            || (name.getTitles().getTitleBefore() == null
                                && name.getTitles().getTitleAfter() == null)));
            assertThat(allLeavesNull)
                    .as("top-level embeddable null on persist must come back null or with every nested leaf null")
                    .isTrue();
        }
    }

    @Test
    void findAllReturnsEveryEntityWithItsEmbeddedPayload() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                em.persist(new UserFixtureEntity(10L, "AcmeA",
                        new NameFixture(new TitlesFixture("Dr.", null), "Alice", "A")));
                em.persist(new UserFixtureEntity(11L, "AcmeB",
                        new NameFixture(null, "Bob", "B")));
            });
        }
        try (var em = context.openSession()) {
            var all = em.findAll(UserFixtureEntity.class);
            assertThat(all).hasSize(2);
            var alice = all.stream().filter(u -> u.getId().equals(10L)).findFirst().orElseThrow();
            var bob = all.stream().filter(u -> u.getId().equals(11L)).findFirst().orElseThrow();
            assertThat(alice.getName().getFirstName()).isEqualTo("Alice");
            assertThat(alice.getName().getTitles().getTitleBefore()).isEqualTo("Dr.");
            assertThat(bob.getName().getFirstName()).isEqualTo("Bob");
        }
    }

    // ------ @EmbeddedId ------

    @Test
    void persistAndFindByEmbeddedIdRoundTripsPayload() throws Exception {
        var pk = new CompositeKeyFixture("A", "1");
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(new CompositeKeyFixtureEntity(pk, "data")));
        }
        try (var em = context.openSession()) {
            var found = em.find(CompositeKeyFixtureEntity.class, pk).orElseThrow();
            assertThat(found.getPayload()).isEqualTo("data");
            assertThat(found.getPk()).isEqualTo(pk);
        }
    }

    @Test
    void findWithUnknownEmbeddedIdReturnsEmpty() throws Exception {
        try (var em = context.openSession()) {
            assertThat(em.find(CompositeKeyFixtureEntity.class,
                    new CompositeKeyFixture("missing", "x"))).isEmpty();
        }
    }

    @Test
    void twoEntitiesWithSamePartAButDifferentPartBCoexist() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> {
                em.persist(new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "1"), "first"));
                em.persist(new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "2"), "second"));
            });
        }
        try (var em = context.openSession()) {
            var all = em.findAll(CompositeKeyFixtureEntity.class);
            assertThat(all).hasSize(2);
            assertThat(all).extracting(e -> e.getPk().getPartB())
                    .containsExactlyInAnyOrder("1", "2");
        }
    }

    @Test
    void findWithKeyThatMatchesOnlyOnePartReturnsEmpty() throws Exception {
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(
                    new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "1"), "d")));
        }
        try (var em = context.openSession()) {
            assertThat(em.find(CompositeKeyFixtureEntity.class, new CompositeKeyFixture("A", "2")))
                    .as("partial key match on part_a must not satisfy a composite-key lookup")
                    .isEmpty();
            assertThat(em.find(CompositeKeyFixtureEntity.class, new CompositeKeyFixture("B", "1")))
                    .as("partial key match on part_b must not satisfy a composite-key lookup")
                    .isEmpty();
        }
    }
}
