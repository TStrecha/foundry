package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.persister.ManagedColumn;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedColumnTest {

    private ManagedColumn<BookEntity, Object> columnFor(String name) throws Exception {
        return new ManagedColumn<>(name, BookEntity.class.getDeclaredField(bookFieldFor(name)));
    }

    private String bookFieldFor(String columnLabel) {
        return switch (columnLabel) {
            case "id" -> "id";
            case "title" -> "title";
            case "author" -> "author";
            case "genre" -> "genre";
            case "page_count" -> "pageCount";
            default -> throw new IllegalArgumentException(columnLabel);
        };
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
    void nullColumnLabelInConstructorIsRejected() throws Exception {
        var field = BookEntity.class.getDeclaredField("title");
        assertThatThrownBy(() -> new ManagedColumn<>(null, field))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankColumnLabelInConstructorIsRejected() throws Exception {
        var field = BookEntity.class.getDeclaredField("title");
        assertThatThrownBy(() -> new ManagedColumn<>("   ", field))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullFieldInConstructorIsRejected() {
        assertThatThrownBy(() -> new ManagedColumn<>("x", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFieldValueFromNullEntityIsRejected() throws Exception {
        var column = columnFor("title");
        assertThatThrownBy(() -> column.getFieldValue(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
