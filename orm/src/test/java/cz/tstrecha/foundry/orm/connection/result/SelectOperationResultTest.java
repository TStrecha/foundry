package cz.tstrecha.foundry.orm.connection.result;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelectOperationResultTest {

    @Test
    void mapToObjectReturnsSingleResult() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        var stmt = mock(Statement.class);

        var result = new SelectOperationResult(stmt, rs);
        var mapped = result.mapToObject(r -> "ok");

        assertThat(mapped).contains("ok");
    }

    @Test
    void mapToObjectReturnsEmptyWhenNoRows() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        var mapped = result.mapToObject(r -> "no");
        assertThat(mapped).isEmpty();
    }

    @Test
    void mapToObjectThrowsIfMultipleRows() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        assertThatThrownBy(() -> result.mapToObject(r -> "x"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mapToObjectsReturnsAllRowsInOrder() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getInt(1)).thenReturn(1, 2, 3);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        var mapped = result.mapToObjects(r -> r.getInt(1));
        assertThat(mapped).containsExactly(1, 2, 3);
    }

    @Test
    void mapToObjectsReturnsEmptyListWhenNoRows() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        var mapped = result.mapToObjects(r -> "x");
        assertThat(mapped).isEqualTo(List.of());
    }

    @Test
    void closeClosesResultSetAndStatement() throws Exception {
        var rs = mock(ResultSet.class);
        var stmt = mock(Statement.class);
        var result = new SelectOperationResult(stmt, rs);
        result.close();
        verify(rs).close();
        verify(stmt).close();
    }

    @Test
    void mapToObjectRejectsNullRowMapper() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        assertThatThrownBy(() -> result.mapToObject(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mapToObjectsRejectsNullRowMapper() {
        var result = new SelectOperationResult(mock(Statement.class), mock(ResultSet.class));
        assertThatThrownBy(() -> result.mapToObjects(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closeStillClosesStatementWhenResultSetCloseFails() throws Exception {
        var rs = mock(ResultSet.class);
        var stmt = mock(Statement.class);
        doThrow(new SQLException("boom")).when(rs).close();
        var result = new SelectOperationResult(stmt, rs);
        try {
            result.close();
        } catch (Exception ignored) {
        }
        verify(stmt).close();
    }

    @Test
    void duplicatesCanBeMappedWithOrder() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getInt(1)).thenReturn(7, 7, 7);
        var result = new SelectOperationResult(mock(Statement.class), rs);
        var mapped = result.mapToObjects(r -> r.getInt(1));
        assertThat(mapped).containsExactly(7, 7, 7);
    }
}
