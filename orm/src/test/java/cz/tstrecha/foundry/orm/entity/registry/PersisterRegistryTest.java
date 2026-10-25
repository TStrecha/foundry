package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.EntitiesMarker;
import cz.tstrecha.foundry.orm.fixtures.good.PersonEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersisterRegistryTest {

    @Test
    void discoversAnnotatedEntitiesInScanningRoots() throws Exception {
        var registry = new PersisterRegistry(List.of(EntitiesMarker.class));
        assertThat(registry.getPersister(BookEntity.class)).isNotNull();
        assertThat(registry.getPersister(PersonEntity.class)).isNotNull();
    }

    @Test
    void alwaysIncludesBuiltinSystemEntities() throws Exception {
        var registry = new PersisterRegistry(List.of(EntitiesMarker.class));
        assertThat(registry.getPersister(ColumnsEntity.class)).isNotNull();
    }

    @Test
    void returnsNullForUnregisteredType() throws Exception {
        var registry = new PersisterRegistry(List.of(EntitiesMarker.class));
        assertThat(registry.getPersister(String.class)).isNull();
    }

    @Test
    void emptyScanningRootsStillRegistersSystemEntities() throws Exception {
        var registry = new PersisterRegistry(List.of());
        assertThat(registry.getPersister(ColumnsEntity.class)).isNotNull();
    }

    @Test
    void nullScanningRootsIsRejected() {
        assertThatThrownBy(() -> new PersisterRegistry(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getPersisterWithNullTypeIsRejected() throws Exception {
        var registry = new PersisterRegistry(List.of(EntitiesMarker.class));
        assertThatThrownBy(() -> registry.getPersister(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
