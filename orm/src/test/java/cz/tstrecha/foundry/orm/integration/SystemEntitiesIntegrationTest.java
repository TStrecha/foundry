package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntityId;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemEntitiesIntegrationTest extends AbstractPostgresIntegrationTest {

    private static FoundryContext context;

    @BeforeAll
    static void setup() throws Exception {
        execute("""
                CREATE TABLE IF NOT EXISTS book (
                    id BIGINT PRIMARY KEY,
                    title TEXT
                );
                """);
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        context = new FoundryContext(config);
    }

    @Test
    void findAllColumnsReturnsAtLeastBookColumns() {
        try (var em = context.openSession()) {
            var columns = em.findAll(ColumnsEntity.class);
            assertThat(columns).extracting(ColumnsEntity::getId).extracting(ColumnsEntityId::getColumnName)
                    .contains("id", "title");
        }
    }
}
