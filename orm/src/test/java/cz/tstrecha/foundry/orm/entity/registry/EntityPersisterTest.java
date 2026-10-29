package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.persister.EntityPersister;
import cz.tstrecha.foundry.orm.entity.persister.ManagedColumn;
import cz.tstrecha.foundry.orm.entity.persister.ManagedType;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityPersisterTest {

    private EntityPersister<BookEntity> bookPersister() throws Exception {
        return new EntityPersister<>(new ManagedType<>(TestDescriptors.descriptorFor(BookEntity.class)));
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
                List.of((ManagedColumn<BookEntity, ?>) persister.getManagedType().getIdColumns().get(0)));
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
    void generateUpdatePreservesColumnOrderDeterministicallyAcrossInvocations() throws Exception {
        var persister = bookPersister();
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("t");
        book.setAuthor("a");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);

        var mt = persister.getManagedType();
        var dirty = List.of(
                mt.getColumnByIndex(1),
                mt.getColumnByIndex(2),
                mt.getColumnByIndex(3));

        var sql1 = persister.generateUpdate(1L, book, dirty).buildSql();
        var sql2 = persister.generateUpdate(1L, book, dirty).buildSql();
        assertThat(sql1)
                .as("generated UPDATE must be deterministic across invocations")
                .isEqualTo(sql2);
    }

    @Test
    void generateUpdateProducesColumnsInListOrder() throws Exception {
        var persister = bookPersister();
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("t");
        book.setAuthor("a");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);

        var mt = persister.getManagedType();
        var titleCol = (ManagedColumn<BookEntity, ?>) mt.getColumnByIndex(1);
        var authorCol = (ManagedColumn<BookEntity, ?>) mt.getColumnByIndex(2);
        var pageCountCol = (ManagedColumn<BookEntity, ?>) mt.getColumnByIndex(4);

        var sql = persister.generateUpdate(
                1L, book, List.of(titleCol, authorCol, pageCountCol)).buildSql();

        var titleIdx = sql.indexOf(titleCol.getColumnLabel());
        var authorIdx = sql.indexOf(authorCol.getColumnLabel());
        var pageCountIdx = sql.indexOf(pageCountCol.getColumnLabel());
        assertThat(titleIdx)
                .as("column order in SET clause must follow the order of the supplied dirty-column list")
                .isLessThan(authorIdx);
        assertThat(authorIdx).isLessThan(pageCountIdx);
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
                List.of(persister.getManagedType().getIdColumns().get(0))))
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

    private EntityPersister<UserFixtureEntity> userPersister() throws Exception {
        return new EntityPersister<>(new ManagedType<>(TestDescriptors.descriptorFor(UserFixtureEntity.class)));
    }

    private EntityPersister<CompositeKeyFixtureEntity> compositePersister() throws Exception {
        return new EntityPersister<>(new ManagedType<>(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class)));
    }

    @Test
    void generateInsertForEmbeddedEntityIncludesAllFlattenedColumns() throws Exception {
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        var sql = userPersister().generateInsert(user).buildSql();

        assertThat(sql)
                .as("INSERT for an entity with @Embedded fields covers every leaf column, not the parent reference")
                .contains("id", "company_name", "first_name", "last_name", "title_before", "title_after");
        assertThat(sql).contains("VALUES (?,?,?,?,?,?)");
    }

    @Test
    void generateSelectByIdForEmbeddedIdProducesWhereClauseOnAllKeyColumns() throws Exception {
        var sql = compositePersister().generateSelectById(new CompositeKeyFixture("A", "B")).buildSql();

        assertThat(sql)
                .as("SELECT ... WHERE for an @EmbeddedId filters by every column of the embedded key")
                .contains("\"part_a\" = ?")
                .contains("\"part_b\" = ?");
    }

    @Test
    void generateSelectByIdForScalarIdIsUnaffectedByEmbeddedIdSupport() throws Exception {
        var sql = bookPersister().generateSelectById(42L).buildSql();

        assertThat(sql).contains(" WHERE \"id\" = ?");
    }

    @Test
    void buildEntityKeyForEmbeddedIdReturnsEmbeddedKeyInstance() throws Exception {
        var persister = compositePersister();
        var entity = new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "B"), "payload");

        var key = persister.buildEntityKey(entity);

        assertThat(key.entityType()).isEqualTo(CompositeKeyFixtureEntity.class);
        assertThat(key.key())
                .as("key for @EmbeddedId is the embeddable instance itself")
                .isEqualTo(new CompositeKeyFixture("A", "B"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void generateUpdateForEmbeddedEntityIncludesNestedColumnInSetClause() throws Exception {
        var persister = userPersister();
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));
        var mt = persister.getManagedType();
        var firstNameCol = (ManagedColumn<UserFixtureEntity, ?>) mt.getColumns().get("first_name");

        var sql = persister.generateUpdate(1L, user, List.of(firstNameCol)).buildSql();

        assertThat(sql)
                .as("UPDATE SET clause for a nested column must refer to the leaf column name, not the embedded parent reference")
                .contains(" SET ")
                .contains("first_name");
    }

    @SuppressWarnings("unchecked")
    @Test
    void generateUpdateForEmbeddedIdEntityWhereClauseContainsAllKeyColumns() throws Exception {
        var persister = compositePersister();
        var entity = new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "B"), "newPayload");
        var mt = persister.getManagedType();
        var payloadCol = (ManagedColumn<CompositeKeyFixtureEntity, ?>) mt.getColumns().get("payload");

        var sql = persister.generateUpdate(new CompositeKeyFixture("A", "B"), entity, List.of(payloadCol)).buildSql();

        assertThat(sql)
                .as("UPDATE ... WHERE for an entity with @EmbeddedId must filter by every key column")
                .contains("\"part_a\" = ?")
                .contains("\"part_b\" = ?");
    }
}
