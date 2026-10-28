package cz.tstrecha.foundry.orm.entity.context;

import cz.tstrecha.foundry.orm.entity.persister.EntityPersister;
import cz.tstrecha.foundry.orm.entity.persister.ManagedType;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.PersonEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistenceContextTest {

    private EntityPersister<BookEntity> bookPersister() throws Exception {
        return new EntityPersister<>(new ManagedType<>(BookEntity.class));
    }

    private BookEntity book(long id) {
        var book = new BookEntity();
        book.setId(id);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);
        return book;
    }

    @Test
    void getByKeyReturnsEmptyForUnknownEntity() {
        assertThat(new PersistenceContext().getByKey(BookEntity.class, 1L)).isEmpty();
    }

    @Test
    void handledEntityCanBeRecoveredByKey() throws Exception {
        var ctx = new PersistenceContext();
        var persister = bookPersister();
        var entity = book(1L);
        ctx.makeEntityHandled(persister, entity);
        var out = ctx.getByKey(BookEntity.class, 1L);
        assertThat(out).isPresent();
        assertThat(out.get().getEntity()).isSameAs(entity);
    }

    @Test
    void makingSameKeyHandledTwiceReplacesTheBag() throws Exception {
        var ctx = new PersistenceContext();
        var persister = bookPersister();
        var a = book(1L);
        var b = book(1L);
        ctx.makeEntityHandled(persister, a);
        ctx.makeEntityHandled(persister, b);
        assertThat(ctx.getByKey(BookEntity.class, 1L).get().getEntity()).isSameAs(b);
    }

    @Test
    void checkForDirtyEntitiesReturnsOnlyMutatedOnes() throws Exception {
        var ctx = new PersistenceContext();
        var persister = bookPersister();
        var clean = book(1L);
        var dirty = book(2L);
        ctx.makeEntityHandled(persister, clean);
        ctx.makeEntityHandled(persister, dirty);
        dirty.setTitle("mutated");

        var dirties = ctx.checkForDirtyEntities();
        assertThat(dirties).hasSize(1);
        assertThat(dirties.get(0).entity()).isSameAs(dirty);
    }

    @Test
    void refreshingAnAlreadyManagedEntityResetsSnapshot() throws Exception {
        var ctx = new PersistenceContext();
        var persister = bookPersister();
        var book = book(1L);
        ctx.makeEntityHandled(persister, book);
        book.setTitle("mutated");
        ctx.makeEntityHandled(persister, book);
        assertThat(ctx.checkForDirtyEntities()).isEmpty();
    }

    @Test
    void getByKeyWithNullTypeIsRejected() {
        assertThatThrownBy(() -> new PersistenceContext().getByKey(null, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getByKeyWithNullKeyIsRejected() {
        assertThatThrownBy(() -> new PersistenceContext().getByKey(BookEntity.class, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void makeEntityHandledWithNullPersisterIsRejected() {
        assertThatThrownBy(() -> new PersistenceContext().makeEntityHandled(null, book(1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void makeEntityHandledWithNullEntityIsRejected() throws Exception {
        var persister = bookPersister();
        assertThatThrownBy(() -> new PersistenceContext().makeEntityHandled(persister, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sameKeyValueAcrossTypesIsIsolated() throws Exception {
        var ctx = new PersistenceContext();
        var bookPersister = bookPersister();
        var personPersister = new EntityPersister<>(new ManagedType<>(PersonEntity.class));

        var b = book(1L);
        var p = new PersonEntity();
        p.setId(1L); p.setFirstName("A"); p.setLastName("B");

        ctx.makeEntityHandled(bookPersister, b);
        ctx.makeEntityHandled(personPersister, p);

        assertThat(ctx.getByKey(BookEntity.class, 1L).orElseThrow().getEntity()).isSameAs(b);
        assertThat(ctx.getByKey(PersonEntity.class, 1L).orElseThrow().getEntity()).isSameAs(p);
    }
}
