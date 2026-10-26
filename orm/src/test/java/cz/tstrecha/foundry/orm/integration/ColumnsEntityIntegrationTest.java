package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the type mapping of the system entity. Several of its columns in
 * information_schema are numeric (e.g. {@code character_maximum_length}) but
 * the entity declares them as {@code String}. This can lead to silent data
 * loss when the DB value exists.
 */
class ColumnsEntityIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setup() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS varchar_table (
                    id BIGINT PRIMARY KEY,
                    label VARCHAR(50)
                );
                """);
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @Test
    void characterMaximumLengthFieldTypeMatchesInformationSchema() {
        try (var em = context.openSession()) {
            var columns = em.findAll(ColumnsEntity.class);
            var labelColumn = columns.stream()
                    .filter(c -> "varchar_table".equals(c.getTableName())
                            && "label".equals(c.getColumnName()))
                    .findFirst().orElseThrow();
            assertThat(labelColumn.getCharacterMaximumLength()).isEqualTo("50");
        }
    }

    @Test
    void bookTableColumnsAreVisibleThroughSystemEntity() {
        try (var em = context.openSession()) {
            var columns = em.findAll(ColumnsEntity.class);
            assertThat(columns).extracting(ColumnsEntity::getColumnName).contains("id", "label");
        }
    }

    @Test
    void findOnColumnsEntityWithUnknownIdReturnsEmpty() {
        try (var em = context.openSession()) {
            var found = em.find(ColumnsEntity.class, "no_such_table_anywhere");
            assertThat(found).isEmpty();
        }
    }
}
