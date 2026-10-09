package cz.tstrecha.foundry.orm.connection.transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private boolean active;
    private boolean rollbackOnly;
    private final Connection connection;

    public Transaction(Connection connection) {
        this.active = false;
        this.connection = connection;
    }

    public void begin() throws SQLException {
        if (active) {
            throw new IllegalStateException("Transaction is already active");
        }
        this.active = true;
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        requireActive();
        if(rollbackOnly) {
            throw new IllegalStateException("Transaction was marked as rollback only");
        }
        connection.commit();
        this.active = false;
    }

    public void rollback() throws SQLException {
        requireActive();
        connection.rollback();
        this.active = false;
    }

    public void markAsRollbackOnly() {
        this.rollbackOnly = true;
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("Transaction is no longer active");
        }
    }
}
