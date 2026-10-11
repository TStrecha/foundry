package cz.tstrecha.foundry.orm.connection.provider;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionProviderTest {

    @Test
    void acquireConnectionCachesTheFirstConnection() throws SQLException {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        var provider = new ConnectionProvider(ds);
        var c1 = provider.acquireConnection();
        var c2 = provider.acquireConnection();

        assertThat(c1).isSameAs(conn);
        assertThat(c2).isSameAs(conn);
        verify(ds).getConnection();
    }

    @Test
    void closeClosesTheCachedConnection() throws SQLException {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        var provider = new ConnectionProvider(ds);
        provider.acquireConnection();
        provider.close();

        verify(conn).close();
    }

    @Test
    void closeBeforeAcquireIsNoop() throws SQLException {
        var ds = mock(DataSource.class);
        var provider = new ConnectionProvider(ds);
        provider.close();
        verify(ds, never()).getConnection();
    }

    @Test
    void closeIsIdempotent() throws SQLException {
        var ds = mock(DataSource.class);
        var conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        var provider = new ConnectionProvider(ds);
        provider.acquireConnection();
        provider.close();
        provider.close();

        verify(conn, times(1)).close();
    }

    @Test
    void nullDataSourceIsRejected() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ConnectionProvider(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dataSourceFailurePropagates() throws SQLException {
        var ds = mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new SQLException("down"));
        var provider = new ConnectionProvider(ds);
        org.assertj.core.api.Assertions.assertThatThrownBy(provider::acquireConnection)
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("down");
    }

    @Test
    void acquireAfterCloseOpensFreshConnection() throws SQLException {
        var ds = mock(DataSource.class);
        var first = mock(Connection.class);
        var second = mock(Connection.class);
        when(ds.getConnection()).thenReturn(first, second);

        var provider = new ConnectionProvider(ds);
        provider.acquireConnection();
        provider.close();
        assertThat(provider.acquireConnection()).isSameAs(second);
    }
}
