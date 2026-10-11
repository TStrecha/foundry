package cz.tstrecha.foundry.orm;

import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoundryContextTest {

    @Test
    void nullConfigurationIsRejected() {
        assertThatThrownBy(() -> new FoundryContext(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidJdbcUrlIsRejectedUpFront() {
        var config = FoundryConfiguration.builder()
                .url("not-a-valid-jdbc-url")
                .user("u")
                .password("p")
                .scanningRoots(List.of(EntitiesMarker.class))
                .build();
        assertThatThrownBy(() -> new FoundryContext(config))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
