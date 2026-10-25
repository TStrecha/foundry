package cz.tstrecha.foundry.orm.connection.transaction;

import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class Transaction {

    @Getter
    private boolean active;
    private boolean rollbackOnly;
    private final Connection connection;

    public Transaction(Connection connection) {
        requireNotNullArgument(connection, "connection");
        this.connection = connection;
        this.active = false;
    }

    public void begin() throws SQLException {
        if (active) {
            throw new IllegalStateException("Transaction is already active");
        }
        connection.setAutoCommit(false);
        this.active = true;
    }

    public void commit() throws SQLException {
        requireActive();
        if(rollbackOnly) {
            throw new IllegalStateException("Transaction was marked as rollback only");
        }
        try {
            connection.commit();
        } finally {
            this.active = false;
            this.rollbackOnly = false;
        }
    }

    public void rollback() throws SQLException {
        requireActive();
        try {
            connection.rollback();
        } finally {
            this.active = false;
            this.rollbackOnly = false;
        }
    }

    public void markAsRollbackOnly() {
        requireActive();
        this.rollbackOnly = true;
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("Transaction is no longer active");
        }
    }
}
