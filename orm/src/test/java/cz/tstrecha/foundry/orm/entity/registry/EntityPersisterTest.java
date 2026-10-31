package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.connection.DBSession;
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
import cz.tstrecha.foundry.orm.fixtures.generated.SequencedWidgetEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.identitygen.IdentityWidgetEntity;
import cz.tstrecha.foundry.orm.fixtures.uuidgen.UuidWidgetEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EntityPersisterTest {

    private EntityPersister<BookEntity> bookPersister() throws Exception {
        return new EntityPersister<>(TestDescriptors.managedTypeFor(BookEntity.class));
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

        var sql = bookPersister().generateInsert(book).sql().buildSql();
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
        return new EntityPersister<>(TestDescriptors.managedTypeFor(UserFixtureEntity.class));
    }

    private EntityPersister<CompositeKeyFixtureEntity> compositePersister() throws Exception {
        return new EntityPersister<>(TestDescriptors.managedTypeFor(CompositeKeyFixtureEntity.class));
    }

    @Test
    void generateInsertForEmbeddedEntityIncludesAllFlattenedColumns() throws Exception {
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        var sql = userPersister().generateInsert(user).sql().buildSql();

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

    private EntityPersister<IdentityWidgetEntity> identityPersister() throws Exception {
        return new EntityPersister<>(TestDescriptors.managedTypeFor(IdentityWidgetEntity.class));
    }

    private EntityPersister<UuidWidgetEntity> uuidPersister() throws Exception {
        return new EntityPersister<>(TestDescriptors.managedTypeFor(UuidWidgetEntity.class));
    }

    private EntityPersister<SequencedWidgetEntity> sequencedPersister() throws Exception {
        return new EntityPersister<>(TestDescriptors.managedTypeFor(SequencedWidgetEntity.class));
    }

    @Test
    void generateInsertForEntityWithoutGeneratorsAdvertisesNoPostInsertColumns() throws Exception {
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setGenre(Genre.FICTION);
        book.setPageCount(1L);

        var strategy = bookPersister().generateInsert(book);

        assertThat(strategy.postInsertGeneratedColumns())
                .as("an entity whose columns are all plain data has nothing to pull back after insert — the strategy must say so with an empty list, so the Basic result path is picked")
                .isEmpty();
        assertThat(strategy.sql().buildSql())
                .as("without post-insert columns the INSERT must not append a RETURNING suffix, otherwise the Basic executor would receive one and switch to the more expensive query path for no reason")
                .doesNotContain("RETURNING");
    }

    @Test
    void generateInsertForIdentityEntityWithNullIdAdvertisesTheIdAsPostInsert() throws Exception {
        var widget = new IdentityWidgetEntity();
        widget.setLabel("alpha");

        var strategy = identityPersister().generateInsert(widget);

        assertThat(strategy.postInsertGeneratedColumns())
                .as("an entity with @GeneratedValue(IDENTITY) and no caller-supplied id depends on the database to produce the id — the persister must declare that column as post-insert so the pipeline wires a RETURNING extractor")
                .hasSize(1);
        assertThat(strategy.postInsertGeneratedColumns().get(0).getColumnLabel()).isEqualTo("id");
        assertThat(strategy.sql().buildSql())
                .as("the SQL must request the id back in one round-trip — issuing the INSERT and separately querying lastval would race against concurrent writers")
                .contains(" RETURNING id");
    }

    @Test
    void generateInsertForIdentityEntityWithNullIdOmitsTheIdFromColumnList() throws Exception {
        var widget = new IdentityWidgetEntity();
        widget.setLabel("alpha");

        var sql = identityPersister().generateInsert(widget).sql().buildSql();

        assertThat(sql)
                .as("INSERT INTO identity_widget(label) VALUES (?) — the id column must be excluded from the explicit column list; passing NULL would fail against GENERATED BY DEFAULT when the backend treats NULL as a literal value, and let the auto-generation path kick in only when the column is omitted entirely")
                .doesNotContain("(id,")
                .doesNotContain(",id)");
    }

    @Test
    void generateInsertForIdentityEntityWithCallerSuppliedIdDoesNotAdvertiseIdAsPostInsert() throws Exception {
        var widget = new IdentityWidgetEntity();
        widget.setId(777L);
        widget.setLabel("preassigned");

        var strategy = identityPersister().generateInsert(widget);

        assertThat(strategy.postInsertGeneratedColumns())
                .as("JPA contract: when the caller supplies the id, the generator must not fire — the persister must therefore not try to read it back either, otherwise RETURNING would return the same id the caller already knows")
                .isEmpty();
        assertThat(strategy.sql().buildSql())
                .as("no post-insert columns to pull back — the SQL must omit the RETURNING clause")
                .doesNotContain("RETURNING");
    }

    @Test
    void generateInsertForIdentityEntityWithCallerSuppliedIdIncludesIdInColumnList() throws Exception {
        var widget = new IdentityWidgetEntity();
        widget.setId(777L);
        widget.setLabel("preassigned");

        var sql = identityPersister().generateInsert(widget).sql().buildSql();

        assertThat(sql)
                .as("a caller-supplied id must be bound into VALUES — otherwise data migrations and deterministic test fixtures could never set the id they need")
                .contains("id");
    }

    @Test
    void generateInsertForUuidEntityAdvertisesNoPostInsertColumns() throws Exception {
        var widget = new UuidWidgetEntity();
        widget.setLabel("alpha");

        var strategy = uuidPersister().generateInsert(widget);

        assertThat(strategy.postInsertGeneratedColumns())
                .as("UUID is a pre-insert strategy — the value is produced in Java before the write, so the persister must not request it back from the database")
                .isEmpty();
        assertThat(strategy.sql().buildSql())
                .as("UUID flow needs no RETURNING clause — adding one would needlessly switch the executor to the query path")
                .doesNotContain("RETURNING");
    }

    @Test
    void generateInsertForSequencedEntityAdvertisesNoPostInsertColumns() throws Exception {
        var widget = new SequencedWidgetEntity();
        widget.setLabel("alpha");

        var strategy = sequencedPersister().generateInsert(widget);

        assertThat(strategy.postInsertGeneratedColumns())
                .as("SEQUENCE values are fetched up-front via nextval and bound into the INSERT — the persister must not advertise them as post-insert, that would duplicate work")
                .isEmpty();
    }

    @Test
    void generateValuesPreInsertFillsNullIdWithUuidGenerator() throws Exception {
        var widget = new UuidWidgetEntity();
        widget.setLabel("alpha");

        uuidPersister().generateValuesPreInsert(widget, mock(DBSession.class));

        assertThat(widget.getId())
                .as("a @GeneratedValue(UUID) field with a null value must be populated by the pre-insert phase — otherwise the INSERT binds NULL into a NOT NULL PK column")
                .isNotNull()
                .isInstanceOf(UUID.class);
    }

    @Test
    void generateValuesPreInsertDoesNotOverrideCallerSuppliedId() throws Exception {
        var widget = new UuidWidgetEntity();
        var preassigned = UUID.randomUUID();
        widget.setId(preassigned);
        widget.setLabel("alpha");

        uuidPersister().generateValuesPreInsert(widget, mock(DBSession.class));

        assertThat(widget.getId())
                .as("JPA contract: when the caller supplied a value, the generator must not fire — otherwise explicit control over ids (data migrations, test fixtures) becomes impossible")
                .isEqualTo(preassigned);
    }

    @Test
    void generateValuesPreInsertAsksSessionForSequenceNextValueOnSequencedEntity() throws Exception {
        var widget = new SequencedWidgetEntity();
        widget.setLabel("alpha");
        var session = mock(DBSession.class);
        when(session.selectNextValueFromSequence(any())).thenReturn(123L);

        sequencedPersister().generateValuesPreInsert(widget, session);

        verify(session).selectNextValueFromSequence(any());
        assertThat(widget.getId())
                .as("the value handed back by the session must land on the entity — otherwise the next step binds NULL and the write fails against the NOT NULL id constraint")
                .isEqualTo(123L);
    }

    @Test
    void generateValuesPreInsertSkipsIdentityColumnsBecauseTheyAreFilledPostInsert() throws Exception {
        var widget = new IdentityWidgetEntity();
        widget.setLabel("alpha");
        var session = mock(DBSession.class);

        identityPersister().generateValuesPreInsert(widget, session);

        assertThat(widget.getId())
                .as("IDENTITY is a post-insert strategy — the pre-insert phase must leave the field null so the Basic/Returning executor path can see 'no value supplied' and let the database generate it")
                .isNull();
        verifyNoInteractions(session);
    }

    @Test
    void generateValuesPreInsertIsAHarmlessNoOpOnEntitiesWithoutGenerators() throws Exception {
        var book = new BookEntity();
        book.setId(5L);
        var session = mock(DBSession.class);

        bookPersister().generateValuesPreInsert(book, session);

        assertThat(book.getId())
                .as("an entity with no @GeneratedValue has no pre-insert work — the caller-supplied id must survive the phase unmodified")
                .isEqualTo(5L);
        verifyNoInteractions(session);
    }

    @Test
    void generateInsertRejectsEntityOfWrongType() throws Exception {
        var persister = bookPersister();
        var widget = new IdentityWidgetEntity();
        widget.setLabel("alpha");
        assertThatThrownBy(() -> persister.generateInsert(widget))
                .as("the persister is bound to one managed type — a foreign entity must be rejected with ClassCastException rather than silently build nonsense SQL")
                .isInstanceOf(ClassCastException.class);
    }
}
