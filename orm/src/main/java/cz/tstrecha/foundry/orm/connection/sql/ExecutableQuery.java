package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.DatabaseOperationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecutableQuery<T extends DatabaseOperationResult> {

    String buildSql();

    void bindParameters(PreparedStatement statement) throws SQLException;

    T executeStatement(PreparedStatement statement) throws SQLException;

    default T execute(Connection connection) throws SQLException {
        var sql = buildSql() + ";";
        System.out.println("Executing query: " + sql);

        var statement = connection.prepareStatement(sql);
        bindParameters(statement);
        return executeStatement(statement);
    }
}
