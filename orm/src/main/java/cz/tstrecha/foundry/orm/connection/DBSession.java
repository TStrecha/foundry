package cz.tstrecha.foundry.orm.connection;

import cz.tstrecha.foundry.orm.entity.EntityManager;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class DBSession {

    private final Connection connection;

    @Getter
    private final EntityManager entityManager;

    @SneakyThrows
    public DBSession(String url, String username, String password, FoundryContext context) {
        this.connection = DriverManager.getConnection(url, username, password);
        this.entityManager = new EntityManager(this, context.getEntityContext());
    }

    @SneakyThrows
    public QueryResult selectAll(String table, String whereClause, String... columns) {
        var sql = new StringBuilder()
                .append(" SELECT ").append(String.join(", ", columns))
                .append(" FROM ").append(table);
        if(whereClause != null) {
            sql
                .append(" WHERE ").append(whereClause);
        }

        var st = connection.prepareStatement(sql.toString());
        var rs = st.executeQuery();

        var metaData = rs.getMetaData();
        var columnCount = rs.getMetaData().getColumnCount();

        var columnNames = extractColumnNames(metaData, columnCount);

        var rows = new LinkedList<LinkedList<String>>();
        while (rs.next()) {
            rows.add(extractNextRow(rs, columnCount));
        }

        rs.close();
        st.close();

        return new QueryResult(columnNames, rows);
    }

    @SneakyThrows
    public SingleQueryResult selectOne(String table, String whereClause, String... columns) {
        var sql = " SELECT " + String.join(", ", columns)
                + " FROM " + table
                + " WHERE " + whereClause;

        var st = connection.prepareStatement(sql);
        var rs = st.executeQuery();

        var metaData = rs.getMetaData();
        var columnCount = rs.getMetaData().getColumnCount();

        var columnNames = extractColumnNames(metaData, columnCount);

        LinkedList<String> row = null;
        while (rs.next()) {
            if(row != null && !row.isEmpty()){
                throw new RuntimeException("More than one row was present.");
            }

            row = extractNextRow(rs, columnCount);
        }

        rs.close();
        st.close();

        return new SingleQueryResult(columnNames, row);
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
