package cz.tstrecha.foundry.orm.connection.result;

import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UpdateOperationResultTest {

    @Test
    void exposesRowsCreated() {
        var result = new UpdateOperationResult(mock(Statement.class), 7);
        assertThat(result.getRowsCreated()).isEqualTo(7);
    }

    @Test
    void closeClosesStatement() throws Exception {
        var statement = mock(Statement.class);
        var result = new UpdateOperationResult(statement, 1);
        result.close();
        verify(statement).close();
    }

    @Test
    void acceptsZeroRows() {
        var result = new UpdateOperationResult(mock(Statement.class), 0);
        org.assertj.core.api.Assertions.assertThat(result.getRowsCreated()).isZero();
    }

    @Test
    void negativeRowCountIsRejected() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new UpdateOperationResult(mock(Statement.class), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
