package cz.tstrecha.foundry.orm.connection.result;

import cz.tstrecha.foundry.orm.operation.result.BasicInsertOperationResult;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class BasicInsertOperationResultTest {

    @Test
    void exposesTheRowsCreatedPassedAtConstruction() {
        var result = new BasicInsertOperationResult(mock(Statement.class), 7);
        assertThat(result.getRowsCreated()).isEqualTo(7);
    }

    @Test
    void acceptsZeroRows() {
        var result = new BasicInsertOperationResult(mock(Statement.class), 0);
        assertThat(result.getRowsCreated())
                .as("INSERT ... that affects zero rows is a legal outcome — the result must still carry zero through the pipeline, not throw")
                .isZero();
    }

    @Test
    void closeClosesTheUnderlyingStatement() throws Exception {
        var statement = mock(Statement.class);
        var result = new BasicInsertOperationResult(statement, 1);

        result.close();

        verify(statement).close();
    }

    @Test
    void fillEntityDoesNothingAndDoesNotInvokeTheConsumer() throws Exception {
        var result = new BasicInsertOperationResult(mock(Statement.class), 1);
        var consumer = mock(cz.tstrecha.foundry.orm.operation.result.ReturningValueConsumer.class);

        result.fillEntity(consumer);

        verifyNoInteractions(consumer);
    }

    @Test
    void fillEntityDoesNothingEvenWhenCalledRepeatedly() throws Exception {
        var result = new BasicInsertOperationResult(mock(Statement.class), 1);
        var consumer = mock(cz.tstrecha.foundry.orm.operation.result.ReturningValueConsumer.class);

        result.fillEntity(consumer);
        result.fillEntity(consumer);
        result.fillEntity(consumer);

        verifyNoInteractions(consumer);
    }

    @Test
    void fillEntityDoesNotMutateTheRowsCreatedCount() throws Exception {
        var result = new BasicInsertOperationResult(mock(Statement.class), 3);
        var consumer = mock(cz.tstrecha.foundry.orm.operation.result.ReturningValueConsumer.class);

        result.fillEntity(consumer);

        assertThat(result.getRowsCreated())
                .as("the Basic variant knows its row count up-front from executeUpdate — fillEntity must not touch it, because there is no RETURNING stream to count against")
                .isEqualTo(3);
    }

    @Test
    void fillEntityNeverTouchesAResultSetBecauseThereIsNone() throws Exception {
        var statement = mock(Statement.class);
        var resultSet = mock(ResultSet.class);
        var result = new BasicInsertOperationResult(statement, 1);

        result.fillEntity(rs -> {});

        verifyNoInteractions(resultSet);
    }
}
