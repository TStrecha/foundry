package cz.tstrecha.foundry.orm.connection;

import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import cz.tstrecha.foundry.orm.connection.sql.InsertSql;
import cz.tstrecha.foundry.orm.connection.sql.SelectSql;
import cz.tstrecha.foundry.orm.connection.transaction.Transaction;
import cz.tstrecha.foundry.orm.entity.row.RowMapperBuilder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DBSession implements Closeable {

    private final ConnectionProvider connectionProvider;
    @Getter
    private final Transaction transaction;

    @SneakyThrows
    public DBSession(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.transaction = new Transaction(connectionProvider.acquireConnection());
    }

    @SneakyThrows
    public <T> List<T> selectAll(SelectSql sql, RowMapperBuilder<T> mapperBuilder) {
        try (var operationResult = sql.execute(connectionProvider.acquireConnection())) {
            var columnLabels = extractColumnNames(operationResult.getResultSet().getMetaData());
            var mappingFunction = mapperBuilder.buildMapper(columnLabels);

            return operationResult.mapToObjects(mappingFunction);
        }
    }

    @SneakyThrows
    public <T> Optional<T> selectOne(SelectSql sql, RowMapperBuilder<T> mapperBuilder) {
        try (var operationResult = sql.execute(connectionProvider.acquireConnection())) {
            var columnLabels = extractColumnNames(operationResult.getResultSet().getMetaData());
            var mappingFunction = mapperBuilder.buildMapper(columnLabels);

            return operationResult.mapToObject(mappingFunction);
        }
    }

    @SneakyThrows
    public int insert(InsertSql sql) {
        try (var operationResult = sql.execute(connectionProvider.acquireConnection())) {
            return operationResult.getRowsCreated();
        }
    }

    private List<String> extractColumnNames(ResultSetMetaData metaData) throws SQLException {
        var columnCount = metaData.getColumnCount();
        var columns = new LinkedList<String>();

        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        return columns;
    }

    @SneakyThrows
    public void close() {
        connectionProvider.close();
    }

}
