package cz.tstrecha.foundry.orm.connection.result;

import cz.tstrecha.foundry.orm.operation.result.ReturningInsertOperationResult;
import cz.tstrecha.foundry.orm.operation.result.ReturningValueConsumer;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReturningInsertOperationResultTest {

    @Test
    void rowsCreatedIsZeroBeforeFillEntityIsCalled() {
        var result = new ReturningInsertOperationResult(mock(Statement.class), mock(ResultSet.class));

        assertThat(result.getRowsCreated())
                .as("the row count is derived from iterating the RETURNING stream — until fillEntity runs there is no stream yet, so the result must honestly report zero")
                .isZero();
    }

    @Test
    void fillEntityIteratesEveryRowOfTheResultSet() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, true, false);

        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);
        var collected = new ArrayList<Integer>();
        int[] counter = {0};
        ReturningValueConsumer consumer = _ -> collected.add(++counter[0]);

        result.fillEntity(consumer);

        assertThat(collected)
                .as("the consumer must be called once per RETURNING row — a multi-row INSERT with three returned rows must feed the consumer three times")
                .containsExactly(1, 2, 3);
    }

    @Test
    void fillEntitySetsRowsCreatedToTheNumberOfReturnedRows() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);

        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);

        result.fillEntity(_ -> {});

        assertThat(result.getRowsCreated())
                .as("rowsCreated is the authoritative count of how many rows the database wrote — it must equal exactly how many rows the RETURNING stream produced, not an inflated or deflated number")
                .isEqualTo(2);
    }

    @Test
    void fillEntityWithEmptyReturningStreamYieldsZeroRows() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);
        var consumer = mock(ReturningValueConsumer.class);

        result.fillEntity(consumer);

        assertThat(result.getRowsCreated())
                .as("INSERT ... ON CONFLICT DO NOTHING can emit an empty RETURNING stream — the result must report zero rows rather than crash or carry a stale count")
                .isZero();
        verify(consumer, org.mockito.Mockito.never()).consume(resultSet);
    }

    @Test
    void fillEntityPassesTheResultSetToTheConsumerForEachRow() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        var consumer = mock(ReturningValueConsumer.class);

        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);
        result.fillEntity(consumer);

        var inOrder = inOrder(consumer);
        inOrder.verify(consumer, org.mockito.Mockito.times(2)).consume(resultSet);
    }

    @Test
    void closeClosesBothStatementAndResultSet() throws Exception {
        var statement = mock(Statement.class);
        var resultSet = mock(ResultSet.class);
        var result = new ReturningInsertOperationResult(statement, resultSet);

        result.close();

        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    void fillEntityPropagatesSqlExceptionsRaisedByTheConsumer() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);

        ReturningValueConsumer boom = _ -> { throw new java.sql.SQLException("boom"); };

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> result.fillEntity(boom))
                .as("if the consumer fails to extract a value — e.g. the RETURNING column type does not match — the error must surface to the caller rather than be swallowed as if the write had succeeded cleanly")
                .isInstanceOf(java.sql.SQLException.class)
                .hasMessage("boom");
    }

    @Test
    void fillEntityWithNullConsumerIsRejected() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        var result = new ReturningInsertOperationResult(mock(Statement.class), resultSet);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> result.fillEntity(null))
                .as("a null consumer is a caller bug — a RETURNING stream with no handler can only lose data; the result must reject the call explicitly rather than NPE deep in the loop")
                .isInstanceOf(IllegalArgumentException.class);
    }
}
