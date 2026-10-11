package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityPersisterTest {

    private EntityPersister<BookEntity> bookPersister() throws Exception {
        return new EntityPersister<>(new ManagedType<>(BookEntity.class));
    }

    @Test
    void generateSelectByIdProducesWhereOnIdColumn() throws Exception {
        var sql = bookPersister().generateSelectById(1L).buildSql();
        assertThat(sql).startsWith("SELECT ");
        assertThat(sql).contains(" FROM book WHERE \"id\" = ?");
    }

    @Test
    void generateSelectHasNoWhereClause() throws Exception {
        var sql = bookPersister().generateSelect().buildSql();
        assertThat(sql).startsWith("SELECT ").doesNotContain(" WHERE ");
        assertThat(sql).contains(" FROM book");
    }

    @Test
    void generateInsertCoversAllColumns() throws Exception {
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);

        var sql = bookPersister().generateInsert(book).buildSql();
        assertThat(sql).startsWith("INSERT INTO book(");
        assertThat(sql).contains(" VALUES (?,?,?,?,?)");
    }

    @Test
    void generateUpdateUsesSuppliedColumnsOnly() throws Exception {
        var persister = bookPersister();
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("New Title");

        var updateSql = persister.generateUpdate(
                1L, book,
                List.of((ManagedColumn<BookEntity, ?>) persister.getManagedType().getIdColumn()));
        var text = updateSql.buildSql();
        assertThat(text).startsWith("UPDATE book SET ").contains(" WHERE \"id\" = ?");
    }

    @Test
    void buildEntityKeyUsesIdValue() throws Exception {
        var book = new BookEntity();
        book.setId(42L);
        var key = bookPersister().buildEntityKey(book);
        assertThat(key.entityType()).isEqualTo(BookEntity.class);
        assertThat(key.key()).isEqualTo(42L);
    }

    @Test
    void generateInsertCastsEntityToManagedType() throws Exception {
        var persister = bookPersister();
        var other = new Object();
        assertThatThrownBy(() -> persister.generateInsert(other))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void nullManagedTypeInConstructorIsRejected() {
        assertThatThrownBy(() -> new EntityPersister<>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateInsertRejectsNullEntity() throws Exception {
        var persister = bookPersister();
        assertThatThrownBy(() -> persister.generateInsert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateUpdateRejectsNullEntity() throws Exception {
        var persister = bookPersister();
        assertThatThrownBy(() -> persister.generateUpdate(1L, null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateUpdateRejectsNullId() throws Exception {
        var persister = bookPersister();
        var book = new BookEntity();
        book.setId(1L);
        assertThatThrownBy(() -> persister.generateUpdate(null, book,
                List.of((ManagedColumn<BookEntity, ?>) persister.getManagedType().getIdColumn())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateUpdateRejectsEmptyColumnList() throws Exception {
        var persister = bookPersister();
        var book = new BookEntity();
        book.setId(1L);
        assertThatThrownBy(() -> persister.generateUpdate(1L, book, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateSelectByIdRejectsNullId() throws Exception {
        var persister = bookPersister();
        assertThatThrownBy(() -> persister.generateSelectById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildEntityKeyRejectsNullEntity() throws Exception {
        var persister = bookPersister();
        assertThatThrownBy(() -> persister.buildEntityKey(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
