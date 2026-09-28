package cz.tstrecha.foundry.orm.connection.executor;

import cz.tstrecha.foundry.orm.connection.sql.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.function.Function;

public interface SqlExecutor {

    <T> T executeQuery(Query query, Connection connection, Function<ResultSet, T> resultSetConverter);
}
