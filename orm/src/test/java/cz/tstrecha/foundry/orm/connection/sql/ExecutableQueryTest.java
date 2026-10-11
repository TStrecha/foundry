package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.DatabaseOperationResult;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutableQueryTest {

    @Test
    void defaultExecuteAppendsSemicolonAndFollowsPrepareBindExecuteOrder() throws Exception {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        var captured = new String[1];
        var result = new DatabaseOperationResult() {
            @Override public void close() throws IOException {}
        };
        ExecutableQuery<DatabaseOperationResult> query = new ExecutableQuery<>() {
            @Override public String buildSql() { return "SELECT 1 FROM t"; }
            @Override public void bindParameters(PreparedStatement statement) {}
            @Override public DatabaseOperationResult executeStatement(PreparedStatement statement) { return result; }
        };

        var out = query.execute(connection);

        assertThat(out).isSameAs(result);
        verify(connection).prepareStatement("SELECT 1 FROM t;");
    }

    @Test
    void bindAndExecuteAreInvokedInExpectedOrder() throws Exception {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        var tracker = mock(PreparedStatement.class); // trackable statement proxy
        ExecutableQuery<DatabaseOperationResult> query = new ExecutableQuery<>() {
            @Override public String buildSql() { return "X"; }
            @Override public void bindParameters(PreparedStatement s) throws SQLException { tracker.setLong(1, 42L); }
            @Override public DatabaseOperationResult executeStatement(PreparedStatement s) throws SQLException {
                tracker.executeQuery();
                return () -> {};
            }
        };
        query.execute(connection);

        InOrder inOrder = inOrder(tracker);
        inOrder.verify(tracker).setLong(1, 42L);
        inOrder.verify(tracker).executeQuery();
    }
}
