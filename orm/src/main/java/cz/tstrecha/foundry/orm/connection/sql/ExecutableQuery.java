package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.DatabaseOperationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecutableQuery<T extends DatabaseOperationResult> {

    PreparedStatement generateStatement(Connection connection) throws SQLException;

    T executeStatement(PreparedStatement statement) throws SQLException;

    default T execute(Connection connection) throws SQLException {
        var statement = generateStatement(connection);
        return executeStatement(statement);
    }
}
