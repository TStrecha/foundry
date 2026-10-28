package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.persister.ManagedColumn;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DirtyEntityBagTest {

    @Test
    void exposesComponentsViaRecordAccessors() {
        var key = new EntityKey<>(BookEntity.class, 1L);
        var entity = new BookEntity();
        List<ManagedColumn<BookEntity, ?>> cols = List.of();
        var bag = new DirtyEntityBag<>(key, entity, cols);

        assertThat(bag.entityKey()).isEqualTo(key);
        assertThat(bag.entity()).isSameAs(entity);
        assertThat(bag.dirtyColumns()).isSameAs(cols);
    }
}
