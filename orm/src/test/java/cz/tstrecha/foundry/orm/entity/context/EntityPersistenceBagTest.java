package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.persister.ManagedType;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
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
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        assertThat(bag.createDirtyEntityBagIfDirty()).isNull();
    }

    @Test
    void mutatedFieldIsDetectedAsDirty() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
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
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
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
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setGenre(Genre.POETRY);
        assertThat(bag.createDirtyEntityBagIfDirty()).isNotNull();
    }

    @Test
    void snapshotRefreshesAfterReturningDirtyBag() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setTitle("Changed");
        assertThat(bag.createDirtyEntityBagIfDirty()).isNotNull();
        assertThat(bag.createDirtyEntityBagIfDirty()).isNull();
    }

    @Test
    void nullEntityInConstructorIsRejected() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        assertThatThrownBy(() ->
                        new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), null, mt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullKeyInConstructorIsRejected() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
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

    @Test
    void mutationFromNullToValueIsDetectedAsDirty() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        book.setAuthor(null);
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setAuthor("filled in");

        var dirty = bag.createDirtyEntityBagIfDirty();

        assertThat(dirty)
                .as("null -> value is the most common first-time write (e.g. setting an optional field after construction) — if dirty-check misses it, the UPDATE never fires and the field never reaches the database")
                .isNotNull();
        assertThat(dirty.dirtyColumns()).extracting(c -> c.getColumnLabel()).contains("author");
    }

    @Test
    void mutationFromValueToNullIsDetectedAsDirty() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);
        book.setAuthor(null);

        var dirty = bag.createDirtyEntityBagIfDirty();

        assertThat(dirty)
                .as("value -> null is a legitimate 'clear' operation (nullable column, user removing data) — missing it would leak stale values that the caller explicitly wiped")
                .isNotNull();
        assertThat(dirty.dirtyColumns()).extracting(c -> c.getColumnLabel()).contains("author");
    }

    @Test
    void mutationOfEmbeddedLeafFieldIsDetectedAsDirty() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));
        var bag = new EntityPersistenceBag<>(new EntityKey<>(UserFixtureEntity.class, 1L), user, mt);

        user.getName().setFirstName("Bob");

        var dirty = bag.createDirtyEntityBagIfDirty();

        assertThat(dirty)
                .as("a mutation on a leaf column inside an @Embedded value must bubble up to dirty-check — otherwise the whole embedded support would be write-once and never see field-level updates")
                .isNotNull();
        assertThat(dirty.dirtyColumns())
                .extracting(c -> c.getColumnLabel())
                .as("exactly the mutated leaf must be flagged — not the whole embeddable's column set, since siblings did not change")
                .containsExactly("first_name");
    }

    @Test
    void replacingWholeEmbeddedParentFlagsEveryLeafThatActuallyChanged() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));
        var bag = new EntityPersistenceBag<>(new EntityKey<>(UserFixtureEntity.class, 1L), user, mt);

        user.setName(new NameFixture(new TitlesFixture("Prof.", "DSc"), "Bob", "Jones"));

        var dirty = bag.createDirtyEntityBagIfDirty();

        assertThat(dirty)
                .as("replacing the whole embeddable instance must still produce leaf-level dirty columns — the UPDATE SQL operates on columns, not nested objects, so the dirty-check must walk through the parent and flag every leaf whose value differs")
                .isNotNull();
        assertThat(dirty.dirtyColumns())
                .extracting(c -> c.getColumnLabel())
                .as("every nested leaf whose value differs must be in the dirty list — otherwise some columns would stay stale after flush")
                .containsExactlyInAnyOrder("first_name", "last_name", "title_before", "title_after");
    }

    @Test
    void mutatingAndThenRevertingBeforeFlushLeavesEntityClean() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);

        book.setTitle("intermediate");
        book.setTitle("Title");

        assertThat(bag.createDirtyEntityBagIfDirty())
                .as("A -> B -> A before flush is a no-op from the database's perspective — flushing it would be a wasted write and could trigger spurious trigger/audit activity. Dirty-check must compare final value to snapshot, not track every intermediate step")
                .isNull();
    }

    @Test
    void twoMutationsBetweenFlushesCollapseToTheLatestValue() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);

        book.setTitle("first change");
        book.setTitle("second change");

        var dirty = bag.createDirtyEntityBagIfDirty();

        assertThat(dirty).isNotNull();
        assertThat(dirty.dirtyColumns())
                .as("intermediate states never leak into dirty-check — a single column mutated twice must still show up as one dirty column (not two, not zero)")
                .extracting(c -> c.getColumnLabel())
                .containsExactly("title");
        assertThat(book.getTitle())
                .as("the entity carries the latest value — that is what the subsequent UPDATE will bind")
                .isEqualTo("second change");
    }

    @Test
    void secondCheckAfterNewMutationStillDetectsIt() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = book();
        var bag = new EntityPersistenceBag<>(new EntityKey<>(BookEntity.class, 1L), book, mt);

        book.setTitle("first");
        assertThat(bag.createDirtyEntityBagIfDirty()).isNotNull();

        book.setAuthor("second");
        var secondCycle = bag.createDirtyEntityBagIfDirty();

        assertThat(secondCycle)
                .as("after one flush cycle, the snapshot refreshes to the new baseline — a subsequent mutation of a different column must still be detected on the next check, otherwise long-lived sessions silently drop updates past the first")
                .isNotNull();
        assertThat(secondCycle.dirtyColumns())
                .extracting(c -> c.getColumnLabel())
                .as("only the newly dirtied column — the previously flushed title is already in the new snapshot and must not show up again")
                .containsExactly("author");
    }
}
