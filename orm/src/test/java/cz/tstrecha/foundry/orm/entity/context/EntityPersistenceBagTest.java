package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.registry.ManagedType;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityPersistenceBagTest {

    private BookEntity book() {
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);
        return book;
    }

    @Test
    void unchangedEntityIsReportedAsClean() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        assertThat(bag.createDirtyEntityBagIfDirty()).isNull();
    }

    @Test
    void mutatedFieldIsDetectedAsDirty() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setTitle("Updated");

        var dirty = bag.createDirtyEntityBagIfDirty();
        assertThat(dirty).isNotNull();
        assertThat(dirty.dirtyColumns())
                .extracting(c -> c.getColumnLabel())
                .contains("title");
    }

    @Test
    void dirtyBagListsAllChangedColumnsEvenWhenMultiple() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setTitle("new");
        book.setAuthor("new");

        var dirty = bag.createDirtyEntityBagIfDirty();
        assertThat(dirty).isNotNull();
        assertThat(dirty.dirtyColumns())
                .extracting(c -> c.getColumnLabel())
                .contains("title", "author");
    }

    @Test
    void enumFieldChangeIsDetected() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setGenre(Genre.POETRY);
        assertThat(bag.createDirtyEntityBagIfDirty()).isNotNull();
    }

    @Test
    void snapshotRefreshesAfterReturningDirtyBag() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setTitle("Changed");
        assertThat(bag.createDirtyEntityBagIfDirty()).isNotNull();
        assertThat(bag.createDirtyEntityBagIfDirty()).isNull();
    }

    @Test
    void snapshotIsStoredAsUnmodifiableList() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        assertThat(bag.getSnapshot()).isUnmodifiable();
    }

    @Test
    void nullEntityInConstructorIsRejected() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        assertThatThrownBy(() ->
                        new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), null, mt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullKeyInConstructorIsRejected() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        assertThatThrownBy(() ->
                        new EntityPersistenceBag<>(null, book(), mt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullManagedTypeInConstructorIsRejected() {
        assertThatThrownBy(() ->
                        new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
