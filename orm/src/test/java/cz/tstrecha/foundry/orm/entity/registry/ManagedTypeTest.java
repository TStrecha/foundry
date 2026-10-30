package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.persister.ManagedType;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.broken.ChildWithInheritedColumnEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.IdWithoutColumnEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.MultipleIdsEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.NoIdEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.NoTableEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.UnsupportedTypeEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.PersonEntity;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedTypeTest {

    @Test
    void readsTableNameFromAnnotation() throws Exception {
        assertThat(TestDescriptors.managedTypeFor(BookEntity.class).getTableName()).isEqualTo("book");
    }

    @Test
    void collectsAnnotatedColumns() throws Exception {
        assertThat(TestDescriptors.managedTypeFor(BookEntity.class).getManagedColumnLabels())
                .containsExactlyInAnyOrder("id", "title", "author", "genre", "page_count");
    }

    @Test
    void identifiesIdColumn() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        assertThat(mt.getIdColumns().get(0).getColumnLabel()).isEqualTo("id");
    }

    @Test
    void extractsKeyFromEntity() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = new BookEntity();
        book.setId(7L);
        assertThat(mt.extractKeyFromEntity(book)).isEqualTo(7L);
    }

    @Test
    void entitySnapshotHasOneValuePerColumn() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("T");
        book.setAuthor("A");
        book.setGenre(Genre.FICTION);
        book.setPageCount(10L);

        List<Object> snapshot = (List<Object>) mt.createEntitySnapshot(book);
        assertThat(snapshot).hasSize(5);
        assertThat(snapshot).containsExactlyInAnyOrder(1L, "T", "A", Genre.FICTION, 10L);
    }

    @Test
    void rowMapperPopulatesAllAnnotatedColumns() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var rs = mock(ResultSet.class);
        when(rs.getLong(1)).thenReturn(5L);
        when(rs.getString(2)).thenReturn("Title");
        when(rs.getString(3)).thenReturn("Author");
        when(rs.getString(4)).thenReturn("FICTION");
        when(rs.getLong(5)).thenReturn(123L);

        var mapper = mt.createRowMapper(List.of("id", "title", "author", "genre", "page_count"));
        BookEntity mapped = mapper.map(rs);

        assertThat(mapped.getId()).isEqualTo(5L);
        assertThat(mapped.getTitle()).isEqualTo("Title");
        assertThat(mapped.getAuthor()).isEqualTo("Author");
        assertThat(mapped.getGenre()).isEqualTo(Genre.FICTION);
        assertThat(mapped.getPageCount()).isEqualTo(123L);
    }

    @Test
    void rowMapperRejectsUnknownColumnLabel() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        assertThatThrownBy(() -> mt.createRowMapper(List.of("not_a_column")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueBindersMatchColumnOrder() throws Exception {
        var mt = TestDescriptors.managedTypeFor(PersonEntity.class);
        var person = new PersonEntity();
        person.setId(1L);
        person.setFirstName("Alice");
        person.setLastName("Smith");

        var binders = mt.getValueBindersForEntity(person);
        assertThat(binders).hasSize(3);
        var stmt = mock(PreparedStatement.class);
        for (int i = 0; i < binders.size(); i++) {
            binders.get(i).bind(stmt, i + 1);
        }
        verify(stmt).setLong(anyInt(), eq(1L));
        verify(stmt).setString(anyInt(), eq("Alice"));
        verify(stmt).setString(anyInt(), eq("Smith"));
    }

    @Test
    void typeWithoutTableAnnotationIsRejected() {
        assertThatThrownBy(() -> TestDescriptors.isolatedManagedTypeFor(NoTableEntity.class))
                .as("an entity class without @Entity cannot be managed — must be rejected with a clear error, not NPE on annotation lookup")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void typeWithoutIdAnnotationIsRejected() {
        assertThatThrownBy(() -> TestDescriptors.isolatedManagedTypeFor(NoIdEntity.class))
                .as("an entity without any @Id column cannot be managed and must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unsupportedTypeFailsAtRegistration() {
        assertThatThrownBy(() -> TestDescriptors.isolatedManagedTypeFor(UnsupportedTypeEntity.class))
                .as("entity with a @Column field whose Java type has no TypeHandler must fail at registration, not at first use")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void entityWithMultipleIdsIsRejected() {
        assertThatThrownBy(() -> TestDescriptors.isolatedManagedTypeFor(MultipleIdsEntity.class))
                .as("more than one @Id on scalar columns is a composite-key setup; use @EmbeddedId instead — must be rejected explicitly, not silently take the first one")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fieldWithIdAnnotationButNoColumnAnnotationIsRejected() {
        assertThatThrownBy(() -> TestDescriptors.isolatedManagedTypeFor(IdWithoutColumnEntity.class))
                .as("@Id on a field without @Column is a programmer mistake — must be surfaced with a field-level error that names @Column, not silently skipped so the entity looks id-less")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inheritedAnnotatedFieldsAreDiscovered() throws Exception {
        var mt = TestDescriptors.isolatedManagedTypeFor(ChildWithInheritedColumnEntity.class);
        assertThat(mt.getManagedColumnLabels()).contains("id", "name");
        assertThat(mt.getIdColumns().get(0).getColumnLabel()).isEqualTo("id");
    }

    @Test
    void managedColumnLabelsAreReturnedInInsertionOrder() throws Exception {
        var mt = TestDescriptors.managedTypeFor(PersonEntity.class);
        assertThat(mt.getManagedColumnLabels())
                .as("column order must be deterministic — otherwise INSERT column list and value binders can misalign")
                .containsExactly("id", "first_name", "last_name");
    }

    @Test
    void nullDescriptorIsRejected() {
        var registry = TestDescriptors.descriptorAndRegistryFor(BookEntity.class).registry();
        assertThatThrownBy(() -> new ManagedType<>(null, registry))
                .as("a null entity descriptor is a caller bug — must be rejected explicitly, not deferred to an NPE deep in the setup")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullRegistryIsRejected() {
        var descriptor = TestDescriptors.descriptorFor(BookEntity.class);
        assertThatThrownBy(() -> new ManagedType<>(descriptor, null))
                .as("a null registry is a caller bug — must be rejected explicitly, not deferred to an NPE when a generator lookup happens later")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void scalarIdEntityExposesOneIdColumn() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);

        assertThat(mt.getIdColumns())
                .as("a scalar @Id entity has exactly one id column")
                .hasSize(1);
        assertThat(mt.getIdColumns().get(0).getColumnLabel()).isEqualTo("id");
    }

    @Test
    void embeddedEntityExposesAllFlattenedLeafColumnLabels() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);

        assertThat(mt.getManagedColumnLabels())
                .as("every leaf @Column reachable through @Embedded appears as its own managed column")
                .containsExactlyInAnyOrder("id", "company_name", "first_name", "last_name", "title_before", "title_after");
    }

    @Test
    void embeddedIdEntityExposesOneIdColumnPerKeyPart() throws Exception {
        var mt = TestDescriptors.managedTypeFor(CompositeKeyFixtureEntity.class);

        assertThat(mt.getIdColumns())
                .as("an @EmbeddedId with N @Column fields yields N id columns")
                .extracting(c -> c.getColumnLabel())
                .containsExactlyInAnyOrder("part_a", "part_b");
    }

    @Test
    void extractKeyFromEntityReturnsEmbeddedKeyInstanceForEmbeddedId() throws Exception {
        var mt = TestDescriptors.managedTypeFor(CompositeKeyFixtureEntity.class);
        var entity = new CompositeKeyFixtureEntity();
        var key = new CompositeKeyFixture("A", "B");
        entity.setPk(key);
        entity.setPayload("x");

        assertThat(mt.extractKeyFromEntity(entity))
                .as("for @EmbeddedId the key is the embeddable instance itself — the ORM uses it to bind each key column")
                .isEqualTo(key);
    }

    @Test
    void rowMapperPopulatesEmbeddedColumnsIntoNestedInstance() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var rs = mock(ResultSet.class);
        when(rs.getLong(1)).thenReturn(7L);
        when(rs.getString(2)).thenReturn("Acme");
        when(rs.getString(3)).thenReturn("Alice");

        var mapper = mt.createRowMapper(List.of("id", "company_name", "first_name"));
        var mapped = mapper.map(rs);

        assertThat(mapped.getId()).isEqualTo(7L);
        assertThat(mapped.getCompanyName()).isEqualTo("Acme");
        assertThat(mapped.getName())
                .as("materialising a nested column must create the missing embeddable")
                .isNotNull();
        assertThat(mapped.getName().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void rowMapperPopulatesDeeplyNestedColumnsIntoDoublyEmbeddedInstance() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("Dr.");

        var mapper = mt.createRowMapper(List.of("title_before"));
        var mapped = mapper.map(rs);

        assertThat(mapped.getName()).isNotNull();
        assertThat(mapped.getName().getTitles()).isNotNull();
        assertThat(mapped.getName().getTitles().getTitleBefore()).isEqualTo("Dr.");
    }

    @Test
    void valueBindersForEmbeddedEntityReadThroughNestedPath() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var user = new UserFixtureEntity(1L, "Acme", new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        var stmt = mock(PreparedStatement.class);
        var columnOrder = mt.getManagedColumnLabels().stream().toList();
        var binders = mt.getValueBindersForEntity(user);
        for (int i = 0; i < binders.size(); i++) {
            binders.get(i).bind(stmt, i + 1);
        }

        verify(stmt).setLong(eq(columnOrder.indexOf("id") + 1), eq(1L));
        verify(stmt).setString(eq(columnOrder.indexOf("company_name") + 1), eq("Acme"));
        verify(stmt).setString(eq(columnOrder.indexOf("first_name") + 1), eq("Alice"));
        verify(stmt).setString(eq(columnOrder.indexOf("last_name") + 1), eq("Smith"));
        verify(stmt).setString(eq(columnOrder.indexOf("title_before") + 1), eq("Dr."));
        verify(stmt).setString(eq(columnOrder.indexOf("title_after") + 1), eq("PhD"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createEntitySnapshotIncludesEmbeddedColumnValues() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var user = new UserFixtureEntity(1L, "Acme", new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        List<Object> snapshot = (List<Object>) mt.createEntitySnapshot(user);

        assertThat(snapshot)
                .as("snapshot carries one value per leaf column, including nested ones")
                .containsExactlyInAnyOrder(1L, "Acme", "Alice", "Smith", "Dr.", "PhD");
    }

    @Test
    @SuppressWarnings("unchecked")
    void snapshotForEntityWithMissingEmbeddedHasNullsForNestedColumns() throws Exception {
        var mt = TestDescriptors.managedTypeFor(UserFixtureEntity.class);
        var user = new UserFixtureEntity(1L, "Acme", null);

        List<Object> snapshot = (List<Object>) mt.createEntitySnapshot(user);

        assertThat(snapshot)
                .as("missing embeddable yields nulls for its nested columns rather than failing")
                .contains(1L, "Acme", null, null, null, null);
    }

    @Test
    void rowMapperForEmbeddedIdEntityAutoInstantiatesEmbeddedKey() throws Exception {
        var mt = TestDescriptors.managedTypeFor(CompositeKeyFixtureEntity.class);
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("A");
        when(rs.getString(2)).thenReturn("B");

        var mapper = mt.createRowMapper(List.of("part_a", "part_b"));
        var mapped = mapper.map(rs);

        assertThat(mapped.getPk())
                .as("loading an entity with @EmbeddedId must auto-instantiate the embedded key, otherwise key columns are silently lost")
                .isNotNull();
        assertThat(mapped.getPk().getPartA()).isEqualTo("A");
        assertThat(mapped.getPk().getPartB()).isEqualTo("B");
    }

    @Test
    void valueBindersForEmbeddedIdEntityExtractKeyColumnValues() throws Exception {
        var mt = TestDescriptors.managedTypeFor(CompositeKeyFixtureEntity.class);
        var entity = new CompositeKeyFixtureEntity(new CompositeKeyFixture("A", "B"), "payload");

        var stmt = mock(PreparedStatement.class);
        var columnOrder = mt.getManagedColumnLabels().stream().toList();
        var binders = mt.getValueBindersForEntity(entity);
        for (int i = 0; i < binders.size(); i++) {
            binders.get(i).bind(stmt, i + 1);
        }

        verify(stmt).setString(eq(columnOrder.indexOf("part_a") + 1), eq("A"));
        verify(stmt).setString(eq(columnOrder.indexOf("part_b") + 1), eq("B"));
        verify(stmt).setString(eq(columnOrder.indexOf("payload") + 1), eq("payload"));
    }

    @Test
    void extractKeyFromNullEntityIsRejected() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);

        assertThatThrownBy(() -> mt.extractKeyFromEntity(null))
                .as("extractKey must reject null entity explicitly rather than NPEing on reflection")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getValueBindersForNullEntityIsRejected() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);

        assertThatThrownBy(() -> mt.getValueBindersForEntity(null))
                .as("getValueBindersForEntity must reject null explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createEntitySnapshotForNullEntityIsRejected() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);

        assertThatThrownBy(() -> mt.createEntitySnapshot(null))
                .as("createEntitySnapshot must reject null explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getColumnByIndexAgreesWithSnapshotOrdering() throws Exception {
        var mt = TestDescriptors.managedTypeFor(BookEntity.class);
        var book = new BookEntity();
        book.setId(1L);
        book.setTitle("T");
        book.setAuthor("A");
        book.setGenre(Genre.POETRY);
        book.setPageCount(99L);

        var snapshot = mt.createEntitySnapshot(book);
        for (int i = 0; i < snapshot.size(); i++) {
            var col = mt.getColumnByIndex(i);
            var value = col.getFieldValue(book);
            assertThat(value).as("snapshot[%s] should match column %s", i, col.getColumnLabel())
                    .isEqualTo(snapshot.get(i));
        }
    }
}
