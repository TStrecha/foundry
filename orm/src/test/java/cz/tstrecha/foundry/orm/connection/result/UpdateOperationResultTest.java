package cz.tstrecha.foundry.orm.connection.result;

import cz.tstrecha.foundry.orm.operation.result.UpdateOperationResult;
import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UpdateOperationResultTest {

    @Test
    void exposesTheRowCountPassedAtConstruction() {
        var result = new UpdateOperationResult(mock(Statement.class), 4);
        assertThat(result.getRowsCreated()).isEqualTo(4);
    }

    @Test
    void acceptsZeroRowsAsALegalUpdate() {
        var result = new UpdateOperationResult(mock(Statement.class), 0);
        assertThat(result.getRowsCreated())
                .as("UPDATE ... WHERE id=? that matches no row returns zero — the result must still expose zero, since the caller decides whether that is an error")
                .isZero();
    }

    @Test
    void closeClosesTheUnderlyingStatement() throws Exception {
        var statement = mock(Statement.class);
        var result = new UpdateOperationResult(statement, 1);

        result.close();

        verify(statement).close();
    }
}
