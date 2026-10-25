package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.broken.NoIdEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.NoTableEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.UnsupportedTypeEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.good.PersonEntity;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedTypeTest {

    @Test
    void readsTableNameFromAnnotation() throws Exception {
        assertThat(new ManagedType<>(BookEntity.class).getTableName()).isEqualTo("book");
    }

    @Test
    void collectsAnnotatedColumns() throws Exception {
        assertThat(new ManagedType<>(BookEntity.class).getManagedColumnLabels())
                .containsExactlyInAnyOrder("id", "title", "author", "genre", "page_count");
    }

    @Test
    void identifiesIdColumn() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        assertThat(mt.getIdColumn().getColumnLabel()).isEqualTo("id");
    }

    @Test
    void extractsKeyFromEntity() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
        var book = new BookEntity();
        book.setId(7L);
        assertThat(mt.extractKeyFromEntity(book)).isEqualTo(7L);
    }

    @Test
    void entitySnapshotHasOneValuePerColumn() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
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
        var mt = new ManagedType<>(BookEntity.class);
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
        var mt = new ManagedType<>(BookEntity.class);
        assertThatThrownBy(() -> mt.createRowMapper(List.of("not_a_column")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueBindersMatchColumnOrder() throws Exception {
        var mt = new ManagedType<>(PersonEntity.class);
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
        verify(stmt).setLong(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.eq(1L));
        verify(stmt).setString(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.eq("Alice"));
        verify(stmt).setString(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.eq("Smith"));
    }

    @Test
    void typeWithoutTableAnnotationIsRejected() {
        assertThatThrownBy(() -> new ManagedType<>(NoTableEntity.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void typeWithoutIdAnnotationIsRejected() {
        assertThatThrownBy(() -> new ManagedType<>(NoIdEntity.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unsupportedTypeFailsAtRegistration() {
        assertThatThrownBy(() -> new ManagedType<>(UnsupportedTypeEntity.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void entityWithMultipleIdsIsRejected() {
        assertThatThrownBy(() -> new ManagedType<>(
                cz.tstrecha.foundry.orm.fixtures.broken.MultipleIdsEntity.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void idAnnotationWithoutColumnAnnotationIsRejected() {
        assertThatThrownBy(() -> new ManagedType<>(
                cz.tstrecha.foundry.orm.fixtures.broken.IdWithoutColumnEntity.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void inheritedAnnotatedFieldsAreDiscovered() throws Exception {
        var mt = new ManagedType<>(
                cz.tstrecha.foundry.orm.fixtures.broken.ChildWithInheritedColumnEntity.class);
        assertThat(mt.getManagedColumnLabels()).contains("id", "name");
        assertThat(mt.getIdColumn().getColumnLabel()).isEqualTo("id");
    }

    @Test
    void managedColumnLabelsAreReturnedInInsertionOrder() throws Exception {
        var mt = new ManagedType<>(cz.tstrecha.foundry.orm.fixtures.good.PersonEntity.class);
        assertThat(mt.getManagedColumnLabels())
                .as("column order must be deterministic — otherwise INSERT column list and value binders can misalign")
                .containsExactly("id", "first_name", "last_name");
    }

    @Test
    void nullTypeIsRejected() {
        assertThatThrownBy(() -> new ManagedType<>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getColumnByIndexAgreesWithSnapshotOrdering() throws Exception {
        var mt = new ManagedType<>(BookEntity.class);
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
