package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.PersonEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityKeyTest {

    @Test
    void equalsConsidersBothTypeAndKey() {
        var a = new EntityKey<>(BookEntity.class, 1L);
        var b = new EntityKey<>(BookEntity.class, 1L);
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void differentTypesWithSameKeyAreNotEqual() {
        var a = new EntityKey<>(BookEntity.class, 1L);
        var b = new EntityKey<>(PersonEntity.class, 1L);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void differentKeysAreNotEqual() {
        var a = new EntityKey<>(BookEntity.class, 1L);
        var b = new EntityKey<>(BookEntity.class, 2L);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void allowsNullKey() {
        var a = new EntityKey<>(BookEntity.class, null);
        var b = new EntityKey<>(BookEntity.class, null);
        assertThat(a).isEqualTo(b);
    }
}
