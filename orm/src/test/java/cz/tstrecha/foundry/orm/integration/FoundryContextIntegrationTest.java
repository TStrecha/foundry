package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryContextIntegrationTest extends AbstractPostgresIntegrationTest {

    @Test
    void openSessionReturnsFreshEntityManagerPerCall() {
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password(password)
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        var context = new FoundryContext(config);
        try (var em1 = context.openSession(); var em2 = context.openSession()) {
            assertThat(em1).isNotSameAs(em2);
        }
    }

    @Test
    void configToStringHidesPassword() {
        var config = FoundryConfiguration.builder()
                .url(jdbcUrl).user(user).password("super-secret-pw")
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        assertThat(config.toString()).doesNotContain("super-secret-pw");
    }
}
