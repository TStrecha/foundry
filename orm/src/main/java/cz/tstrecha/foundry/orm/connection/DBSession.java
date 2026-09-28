package cz.tstrecha.foundry.orm.connection;

import cz.tstrecha.foundry.orm.connection.sql.SelectSql;
import cz.tstrecha.foundry.orm.entity.EntityManager;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;

public class DBSession implements Closeable {

    private final Connection connection;

    @Getter
    private final EntityManager entityManager;

    @SneakyThrows
    public DBSession(String url, String username, String password, FoundryContext context) {
        this.connection = DriverManager.getConnection(url, username, password);
        this.entityManager = new EntityManager(this, context.getEntityContext());
    }

    @SneakyThrows
    public QueryResult selectAll(SelectSql selectSql) {
        return selectSql.execute(connection, this::parseQueryResult);
    }

    @SneakyThrows
    public SingleQueryResult selectOne(SelectSql selectSql) {
        return selectSql.execute(connection, this::parseSingleQueryResult);
    }

    @SneakyThrows
    private SingleQueryResult parseSingleQueryResult(ResultSet resultSet) {
        var metaData = resultSet.getMetaData();
        var columnCount = resultSet.getMetaData().getColumnCount();

        var columnNames = extractColumnNames(metaData, columnCount);

        LinkedList<String> row = null;
        while (resultSet.next()) {
            if(row != null && !row.isEmpty()){
                throw new RuntimeException("More than one row was present.");
            }

            row = extractNextRow(resultSet, columnCount);
        }

        return new SingleQueryResult(columnNames, row);
    }

    @SneakyThrows
    private QueryResult parseQueryResult(ResultSet resultSet) {
        var metaData = resultSet.getMetaData();
        var columnCount = resultSet.getMetaData().getColumnCount();

        var columnNames = extractColumnNames(metaData, columnCount);

        var rows = new LinkedList<LinkedList<String>>();
        while (resultSet.next()) {
            rows.add(extractNextRow(resultSet, columnCount));
        }

        resultSet.close();
        resultSet.close();

        return new QueryResult(columnNames, rows);
    }

    private LinkedList<String> extractNextRow(ResultSet resultSet, int columnCount) throws SQLException {
        var row = new LinkedList<String>();
        for(int i = 1; i <= columnCount; i++) {
            row.add(resultSet.getString(i));
        }

        return row;
    }

    private LinkedList<String> extractColumnNames(ResultSetMetaData metaData, int columnCount) throws SQLException {
        var columns = new LinkedList<String>();

        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        return columns;
    }

    @SneakyThrows
    public void close() {
        connection.close();
    }

}
