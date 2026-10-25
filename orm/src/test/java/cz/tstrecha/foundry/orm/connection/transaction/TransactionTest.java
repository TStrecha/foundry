package cz.tstrecha.foundry.orm.connection.transaction;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionTest {

    @Test
    void beginDisablesAutoCommit() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        verify(connection).setAutoCommit(false);
    }

    @Test
    void beginTwiceThrowsIllegalState() throws SQLException {
        var tx = new Transaction(mock(Connection.class));
        tx.begin();
        assertThatThrownBy(tx::begin).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commitDelegatesToConnection() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        tx.commit();
        verify(connection).commit();
    }

    @Test
    void commitWithoutBeginThrows() {
        var tx = new Transaction(mock(Connection.class));
        assertThatThrownBy(tx::commit).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rollbackDelegatesToConnection() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        tx.rollback();
        verify(connection).rollback();
    }

    @Test
    void rollbackWithoutBeginThrows() {
        var tx = new Transaction(mock(Connection.class));
        assertThatThrownBy(tx::rollback).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commitAfterMarkAsRollbackOnlyThrows() throws SQLException {
        var tx = new Transaction(mock(Connection.class));
        tx.begin();
        tx.markAsRollbackOnly();
        assertThatThrownBy(tx::commit).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markAsRollbackOnlyBeforeBeginIsRejected() {
        var tx = new Transaction(mock(Connection.class));
        assertThatThrownBy(tx::markAsRollbackOnly).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rollbackOnlyFlagDoesNotLeakIntoNextTransaction() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        tx.markAsRollbackOnly();
        tx.rollback();
        tx.begin();
        assertThatCode(tx::commit).doesNotThrowAnyException();
    }

    @Test
    void doubleCommitWithoutReBeginThrows() throws SQLException {
        var tx = new Transaction(mock(Connection.class));
        tx.begin();
        tx.commit();
        assertThatThrownBy(tx::commit).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failingCommitLeavesTransactionRestartable() throws SQLException {
        var connection = mock(Connection.class);
        doThrow(new SQLException("boom")).when(connection).commit();
        var tx = new Transaction(connection);
        tx.begin();
        assertThatThrownBy(tx::commit).isInstanceOf(SQLException.class);
        assertThatCode(tx::begin).doesNotThrowAnyException();
    }

    @Test
    void failingBeginLeavesTransactionInactive() throws SQLException {
        var connection = mock(Connection.class);
        doThrow(new SQLException("fail")).when(connection).setAutoCommit(anyBoolean());
        var tx = new Transaction(connection);
        assertThatThrownBy(tx::begin).isInstanceOf(SQLException.class);
        assertThat(tx.isActive()).isFalse();
    }

    @Test
    void nullConnectionIsRejected() {
        assertThatThrownBy(() -> new Transaction(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void beginAfterCommitStartsNewTransaction() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        tx.commit();
        assertThatCode(tx::begin).doesNotThrowAnyException();
    }

    @Test
    void beginAfterRollbackStartsNewTransaction() throws SQLException {
        var connection = mock(Connection.class);
        var tx = new Transaction(connection);
        tx.begin();
        tx.rollback();
        assertThatCode(tx::begin).doesNotThrowAnyException();
    }

    @Test
    void failingRollbackLeavesTransactionRestartable() throws SQLException {
        var connection = mock(Connection.class);
        doThrow(new SQLException("rb-fail")).when(connection).rollback();
        var tx = new Transaction(connection);
        tx.begin();
        assertThatThrownBy(tx::rollback).isInstanceOf(SQLException.class);
        assertThatCode(tx::begin).doesNotThrowAnyException();
    }
}
