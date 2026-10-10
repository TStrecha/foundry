package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.UpdateOperationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InsertSql implements ExecutableQuery<UpdateOperationResult> {

    private String table;
    private Collection<String> columns;
    private List<ParameterBinder> valueBinders;

    public InsertSql into(String table, Collection<String> columns) {
        this.table = table;
        this.columns = columns;
        return this;
    }
    public InsertSql values(List<ParameterBinder> valueBinders) {
        this.valueBinders = valueBinders;
        return this;
    }

    @Override
    public String buildSql() {
        var columnStatement = String.join(",", columns);
        var valuePlaceholders = valueBinders.stream().map(_ -> "?").collect(Collectors.joining(","));

        var sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(table).append("(").append(columnStatement).append(")");
        sqlBuilder.append(" VALUES ").append("(").append(valuePlaceholders).append(")");

        return sqlBuilder.toString();
    }

    @Override
    public void bindParameters(PreparedStatement statement) throws SQLException {
        for(int i = 0; i < valueBinders.size(); i++) {
            var valueBinder = valueBinders.get(i);
            valueBinder.bind(statement, i + 1);
        }
    }

    @Override
    public UpdateOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new UpdateOperationResult(statement, statement.executeUpdate());
    }
}
