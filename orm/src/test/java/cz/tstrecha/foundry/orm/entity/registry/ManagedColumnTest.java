package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.descriptor.PathFinder;
import cz.tstrecha.foundry.orm.entity.persister.ManagedColumn;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedColumnTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ManagedColumn<BookEntity, Object> columnFor(String columnName) {
        var descriptor = TestDescriptors.descriptorFor(BookEntity.class);
        for (var cp : PathFinder.buildColumnPaths(descriptor)) {
            if (cp.field().columnName().equals(columnName)) {
                return new ManagedColumn(cp);
            }
        }
        throw new IllegalArgumentException("No such column: " + columnName);
    }

    @Test
    void exposesColumnLabelAndFieldType() throws Exception {
        var column = columnFor("title");
        assertThat(column.getColumnLabel()).isEqualTo("title");
        assertThat(column.getFieldType()).isEqualTo(String.class);
    }

    @Test
    void getFieldValueReadsReflectively() throws Exception {
        var book = new BookEntity();
        book.setTitle("Dune");
        var column = columnFor("title");
        assertThat(column.getFieldValue(book)).isEqualTo("Dune");
    }

    @Test
    void binderForEntityWritesCurrentFieldValueToStatement() throws Exception {
        var column = columnFor("title");
        var book = new BookEntity();
        book.setTitle("Hyperion");
        var stmt = mock(PreparedStatement.class);
        column.binderForEntity(book).bind(stmt, 4);
        verify(stmt).setString(4, "Hyperion");
    }

    @Test
    void binderForValueWritesExplicitValue() throws Exception {
        var column = columnFor("title");
        var stmt = mock(PreparedStatement.class);
        column.binderForValue("Snow Crash").bind(stmt, 1);
        verify(stmt).setString(1, "Snow Crash");
    }

    @Test
    void binderForAnyValueCastsThroughFieldType() throws Exception {
        var column = columnFor("id");
        var stmt = mock(PreparedStatement.class);
        column.binderForAnyValue(42L).bind(stmt, 2);
        verify(stmt).setLong(2, 42L);
    }

    @Test
    void binderForAnyValueRejectsWrongType() throws Exception {
        var column = columnFor("id");
        assertThatThrownBy(() -> column.binderForAnyValue("not-a-long").bind(mock(PreparedStatement.class), 1))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void saturateColumnWritesParsedValueBackToEntity() throws Exception {
        var column = columnFor("title");
        var book = new BookEntity();
        var rs = mock(ResultSet.class);
        when(rs.getString(3)).thenReturn("Blindsight");
        column.saturateColumnForEntity(book, rs, 3);
        assertThat(book.getTitle()).isEqualTo("Blindsight");
    }

    @Test
    void saturateEnumColumnPopulatesEnumFromString() throws Exception {
        var column = columnFor("genre");
        var book = new BookEntity();
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("FICTION");
        column.saturateColumnForEntity(book, rs, 1);
        assertThat(book.getGenre()).isEqualTo(Genre.FICTION);
    }

    @Test
    void binderForEntityWithNullLongFieldBindsSqlNull() throws Exception {
        var column = columnFor("page_count");
        var book = new BookEntity();
        var stmt = mock(PreparedStatement.class);
        column.binderForEntity(book).bind(stmt, 1);
        verify(stmt).setNull(1, Types.BIGINT);
    }

    @Test
    void binderForAnyValueWithNullBindsSqlNull() throws Exception {
        var column = columnFor("id");
        var stmt = mock(PreparedStatement.class);
        column.binderForAnyValue(null).bind(stmt, 1);
        verify(stmt).setNull(1, Types.BIGINT);
    }

    @Test
    void nullManagedColumnDescriptorInConstructorIsRejected() {
        assertThatThrownBy(() -> new ManagedColumn<>(null))
                .as("a null descriptor can only be a caller bug — must be rejected explicitly, not deferred to an NPE")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFieldValueFromNullEntityIsRejected() throws Exception {
        var column = columnFor("title");
        assertThatThrownBy(() -> column.getFieldValue(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ManagedColumn<UserFixtureEntity, Object> userColumnFor(String columnName) {
        var descriptor = TestDescriptors.descriptorFor(UserFixtureEntity.class);
        for (var cp : PathFinder.buildColumnPaths(descriptor)) {
            if (cp.field().columnName().equals(columnName)) {
                return new ManagedColumn(cp);
            }
        }
        throw new IllegalArgumentException("No such column: " + columnName);
    }

    @Test
    void getFieldValueOnEmbeddedColumnReadsThroughNestedPath() {
        var column = userColumnFor("first_name");
        var user = new UserFixtureEntity(1L, "Acme", new NameFixture(null, "Alice", "Smith"));

        assertThat(column.getFieldValue(user)).isEqualTo("Alice");
    }

    @Test
    void getFieldValueOnDeeplyNestedColumnWalksThroughAllIntermediates() {
        var column = userColumnFor("title_before");
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        assertThat(column.getFieldValue(user)).isEqualTo("Dr.");
    }

    @Test
    void getFieldValueReturnsNullWhenIntermediateEmbeddedIsMissing() {
        var column = userColumnFor("first_name");
        var user = new UserFixtureEntity(1L, "Acme", null);

        assertThat(column.getFieldValue(user))
                .as("missing embeddable short-circuits read to null")
                .isNull();
    }

    @Test
    void binderForEntityOnEmbeddedColumnBindsNestedValue() throws Exception {
        var column = userColumnFor("first_name");
        var user = new UserFixtureEntity(1L, "Acme", new NameFixture(null, "Alice", "Smith"));
        var stmt = mock(PreparedStatement.class);

        column.binderForEntity(user).bind(stmt, 3);

        verify(stmt).setString(3, "Alice");
    }

    @Test
    void saturateColumnOnEmbeddedColumnAutoInstantiatesMissingParent() throws Exception {
        var column = userColumnFor("first_name");
        var user = new UserFixtureEntity();
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("Alice");

        column.saturateColumnForEntity(user, rs, 1);

        assertThat(user.getName())
                .as("saturating a nested column must create the missing embeddable rather than silently dropping the value")
                .isNotNull();
        assertThat(user.getName().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void saturateColumnOnDeeplyNestedColumnAutoInstantiatesAllIntermediates() throws Exception {
        var column = userColumnFor("title_before");
        var user = new UserFixtureEntity();
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("Dr.");

        column.saturateColumnForEntity(user, rs, 1);

        assertThat(user.getName()).isNotNull();
        assertThat(user.getName().getTitles()).isNotNull();
        assertThat(user.getName().getTitles().getTitleBefore()).isEqualTo("Dr.");
    }

    @Test
    void binderForEntityWithNullEntityIsRejected() {
        var column = columnFor("title");

        assertThatThrownBy(() -> column.binderForEntity(null))
                .as("binderForEntity(null) is a caller bug and must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saturateColumnForEntityWithNullEntityIsRejected() {
        var column = columnFor("title");
        var rs = mock(ResultSet.class);

        assertThatThrownBy(() -> column.saturateColumnForEntity(null, rs, 1))
                .as("saturating into a null entity is a caller bug and must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saturateColumnForEntityWithNullResultSetIsRejected() {
        var column = columnFor("title");
        var book = new BookEntity();

        assertThatThrownBy(() -> column.saturateColumnForEntity(book, null, 1))
                .as("saturating from a null ResultSet is a caller bug and must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saturateColumnReadsNullFromResultSetAndWritesNullIntoEntity() throws Exception {
        var column = columnFor("title");
        var book = new BookEntity();
        book.setTitle("existing");
        var rs = mock(ResultSet.class);
        when(rs.getString(3)).thenReturn(null);

        column.saturateColumnForEntity(book, rs, 3);

        assertThat(book.getTitle())
                .as("SQL NULL in the ResultSet must land as Java null, overwriting any existing value")
                .isNull();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ManagedColumn<Object, Object> scalarIdColumn() {
        var descriptor = TestDescriptors.descriptorFor(BookEntity.class);
        var idPath = PathFinder.buildIdPaths(descriptor).get(0);
        return new ManagedColumn(idPath);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ManagedColumn<Object, Object> embeddedIdColumn(String columnName) {
        var descriptor = TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class);
        for (var cp : PathFinder.buildIdPaths(descriptor)) {
            if (cp.field().columnName().equals(columnName)) {
                return new ManagedColumn(cp);
            }
        }
        throw new IllegalArgumentException("No such id column: " + columnName);
    }

    @Test
    void binderForRootOnTopLevelEntityPathReadsLeafValueFromEntity() throws Exception {
        var column = columnFor("title");
        var book = new BookEntity();
        book.setTitle("Hyperion");
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(book).bind(stmt, 1);

        verify(stmt)
                .setString(1, "Hyperion");
    }

    @Test
    void binderForRootOnNestedEmbeddedPathWalksThroughParentsToLeaf() throws Exception {
        var column = userColumnFor("title_before");
        var user = new UserFixtureEntity(1L, "Acme",
                new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(user).bind(stmt, 4);

        verify(stmt).setString(4, "Dr.");
    }

    @Test
    void binderForRootOnRootValuePathBindsTheRootItselfAsColumnValue() throws Exception {
        var column = scalarIdColumn();
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(42L).bind(stmt, 1);

        verify(stmt)
                .setLong(1, 42L);
    }

    @Test
    void binderForRootOnEmbeddedIdColumnExtractsKeyColumnValueFromKeyInstance() throws Exception {
        var column = embeddedIdColumn("part_a");
        var key = new CompositeKeyFixture("A-value", "B-value");
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(key).bind(stmt, 1);

        verify(stmt).setString(1, "A-value");
    }

    @Test
    void binderForRootWithNullIntermediateEmbeddedBindsSqlNull() throws Exception {
        var column = userColumnFor("first_name");
        var user = new UserFixtureEntity(1L, "Acme", null);
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(user).bind(stmt, 1);

        verify(stmt).setNull(1, JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    void binderForRootDoesNotCastValueDirectly() throws Exception {
        var column = scalarIdColumn();
        var stmt = mock(PreparedStatement.class);

        column.binderForRoot(99L).bind(stmt, 2);

        verify(stmt)
                .setLong(2, 99L);
    }

    @Test
    void binderForRootOnEntityRootedColumnWithWrongRootTypeIsRejected() {
        var column = columnFor("id");

        assertThatThrownBy(() -> column.binderForRoot(42L).bind(mock(PreparedStatement.class), 1))
                .as("entity-rooted path cannot walk a raw primitive value; the caller is confused about which binder to use and must get a clear failure")
                .isInstanceOfAny(IllegalArgumentException.class, ClassCastException.class);
    }

    @Test
    void saturateColumnOnEmbeddedColumnPreservesExistingEmbeddedParent() throws Exception {
        var column = userColumnFor("first_name");
        var existingName = new NameFixture(new TitlesFixture("Dr.", "PhD"), "old", "last");
        var user = new UserFixtureEntity();
        user.setName(existingName);
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("Alice");

        column.saturateColumnForEntity(user, rs, 1);

        assertThat(user.getName())
                .as("existing embeddable must not be replaced when saturating a nested column")
                .isSameAs(existingName);
        assertThat(user.getName().getFirstName()).isEqualTo("Alice");
        assertThat(user.getName().getTitles().getTitleBefore())
                .as("sibling values inside the preserved embeddable must survive")
                .isEqualTo("Dr.");
    }
}
