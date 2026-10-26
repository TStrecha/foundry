package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import cz.tstrecha.foundry.orm.fixtures.good.TypesEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TypesRoundtripIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setupSchema() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS types_entity (
                    id BIGINT PRIMARY KEY,
                    uuid_col UUID,
                    decimal_col NUMERIC(20, 4),
                    integer_col INTEGER,
                    double_col DOUBLE PRECISION,
                    boolean_col BOOLEAN,
                    bytes_col BYTEA
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
        execute("TRUNCATE types_entity;");
    }

    private TypesEntity fullEntity(long id) {
        var e = new TypesEntity();
        e.setId(id);
        e.setUuidCol(UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        e.setDecimalCol(new BigDecimal("12345.6789"));
        e.setIntegerCol(42);
        e.setDoubleCol(3.14159);
        e.setBooleanCol(true);
        e.setBytesCol(new byte[]{1, 2, 3, 4, 5});
        return e;
    }

    @Test
    void uuidRoundtripsIntact() throws Exception {
        var original = fullEntity(1L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 1L).orElseThrow();
            assertThat(loaded.getUuidCol()).isEqualTo(original.getUuidCol());
        }
    }

    @Test
    void bigDecimalRoundtripsIntact() throws Exception {
        var original = fullEntity(2L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 2L).orElseThrow();
            assertThat(loaded.getDecimalCol()).isEqualByComparingTo(original.getDecimalCol());
        }
    }

    @Test
    void integerRoundtripsIntact() throws Exception {
        var original = fullEntity(3L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 3L).orElseThrow();
            assertThat(loaded.getIntegerCol()).isEqualTo(original.getIntegerCol());
        }
    }

    @Test
    void doubleRoundtripsIntact() throws Exception {
        var original = fullEntity(4L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 4L).orElseThrow();
            assertThat(loaded.getDoubleCol()).isEqualTo(original.getDoubleCol());
        }
    }

    @Test
    void booleanRoundtripsIntact() throws Exception {
        var original = fullEntity(5L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 5L).orElseThrow();
            assertThat(loaded.getBooleanCol()).isEqualTo(original.getBooleanCol());
        }
    }

    @Test
    void byteArrayRoundtripsIntact() throws Exception {
        var original = fullEntity(6L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(original));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 6L).orElseThrow();
            assertThat(loaded.getBytesCol()).containsExactly(original.getBytesCol());
        }
    }

    @Test
    void allNullableColumnsCanBeNull() throws Exception {
        var entity = new TypesEntity();
        entity.setId(10L);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(entity));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 10L).orElseThrow();
            assertThat(loaded.getUuidCol()).isNull();
            assertThat(loaded.getDecimalCol()).isNull();
            assertThat(loaded.getIntegerCol()).isNull();
            assertThat(loaded.getDoubleCol()).isNull();
            assertThat(loaded.getBooleanCol()).isNull();
            assertThat(loaded.getBytesCol()).isNull();
        }
    }

    @Test
    void bigDecimalPrecisionIsPreservedAtScaleLimit() throws Exception {
        var entity = new TypesEntity();
        entity.setId(20L);
        entity.setDecimalCol(new BigDecimal("0.0001"));
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(entity));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 20L).orElseThrow();
            assertThat(loaded.getDecimalCol()).isEqualByComparingTo("0.0001");
        }
    }

    @Test
    void emptyByteArrayRoundtripsAsEmpty() throws Exception {
        var entity = new TypesEntity();
        entity.setId(21L);
        entity.setBytesCol(new byte[0]);
        try (var em = context.openSession()) {
            em.runInTransaction(tx -> em.persist(entity));
        }
        try (var em = context.openSession()) {
            var loaded = em.find(TypesEntity.class, 21L).orElseThrow();
            assertThat(loaded.getBytesCol()).isEmpty();
        }
    }
}
