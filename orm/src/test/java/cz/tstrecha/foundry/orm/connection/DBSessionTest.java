package cz.tstrecha.foundry.orm.connection;

import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.sql.InsertSql;
import cz.tstrecha.foundry.orm.connection.sql.SelectSql;
import cz.tstrecha.foundry.orm.connection.sql.UpdateSql;
import cz.tstrecha.foundry.orm.entity.row.RowMapperBuilder;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DBSessionTest {

    @Test
    void selectAllRunsSqlAndMapsAllRows() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        var stmt = mock(PreparedStatement.class);
        var rs = mock(ResultSet.class);
        var md = mock(ResultSetMetaData.class);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.getMetaData()).thenReturn(md);
        when(md.getColumnCount()).thenReturn(1);
        when(md.getColumnLabel(1)).thenReturn("value");
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt(1)).thenReturn(10, 20);

        var session = new DBSession(new ConnectionProvider(ds));
        var select = new SelectSql(List.of("value")).from("t");
        RowMapperBuilder<Integer> builder = columns -> r -> r.getInt(1);
        var result = session.selectAll(select, builder);

        assertThat(result).containsExactly(10, 20);
    }

    @Test
    void selectOneReturnsOptional() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        var stmt = mock(PreparedStatement.class);
        var rs = mock(ResultSet.class);
        var md = mock(ResultSetMetaData.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.getMetaData()).thenReturn(md);
        when(md.getColumnCount()).thenReturn(1);
        when(md.getColumnLabel(1)).thenReturn("v");
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt(1)).thenReturn(42);

        var session = new DBSession(new ConnectionProvider(ds));
        var select = new SelectSql(List.of("v")).from("t");
        RowMapperBuilder<Integer> builder = columns -> r -> r.getInt(1);
        var result = session.selectOne(select, builder);
        assertThat(result).contains(42);
    }

    @Test
    void insertReturnsRowsCreated() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        var stmt = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(3);

        var session = new DBSession(new ConnectionProvider(ds));
        var insert = new InsertSql().into("t", List.of("a")).values(List.of((s, i) -> s.setLong(i, 1L)));
        var result = session.insert(insert);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void updateReturnsRowsAffected() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        var stmt = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        var session = new DBSession(new ConnectionProvider(ds));
        var update = new UpdateSql("t").set(Map.of("a", (s, i) -> s.setLong(i, 1L)));
        assertThat(session.update(update)).isEqualTo(1);
    }

    @Test
    void closeDelegatesToProvider() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var provider = new ConnectionProvider(ds);
        var session = new DBSession(provider);
        session.close();
        verify(conn).close();
    }

    @Test
    void closeRollsBackOpenTransaction() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        session.getTransaction().begin();
        session.close();
        verify(conn).rollback();
    }

    @Test
    void nullProviderIsRejected() {
        assertThatThrownBy(() -> new DBSession(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void selectAllRejectsNullSql() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        assertThatThrownBy(() -> session.selectAll(null, columns -> r -> null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void selectAllRejectsNullMapperBuilder() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        var select = new SelectSql(List.of("v")).from("t");
        assertThatThrownBy(() -> session.selectAll(select, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insertRejectsNullSql() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        assertThatThrownBy(() -> session.insert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsNullSql() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        assertThatThrownBy(() -> session.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closingSessionClosesResourcesEvenWhenNoQueriesExecuted() throws Exception {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
        var session = new DBSession(new ConnectionProvider(ds));
        session.close();
        verify(conn).close();
    }
}
