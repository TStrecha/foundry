package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.executor.SqlExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.function.Function;

public interface ExecutableQuery {

    Query generateQuery();

    SqlExecutor provideExecutor();

    default <T> T execute(Connection connection, Function<ResultSet, T> resultSetConverter) {
        return provideExecutor().executeQuery(generateQuery(), connection, resultSetConverter);
    }
}
