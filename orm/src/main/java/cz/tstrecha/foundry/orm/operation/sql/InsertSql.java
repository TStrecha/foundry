package cz.tstrecha.foundry.orm.operation.sql;

import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import cz.tstrecha.foundry.orm.operation.result.UpdateOperationResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.*;

public class InsertSql implements ExecutableQuery<UpdateOperationResult> {

    private String table;
    private Collection<String> columns;
    private List<ParameterBinder> valueBinders;

    public InsertSql into(String table, Collection<String> columns) {
        requireNotEmptyArgument(table, "table");
        requireValidSqlIdentifier(table, "table");

        requireNotNullArgument(columns, "fields");
        columns.forEach(column -> requireValidSqlIdentifier(column, "column"));

        this.table = table;
        this.columns = columns;
        return this;
    }

    public InsertSql values(List<ParameterBinder> valueBinders) {
        requireNotNullArgument(valueBinders, "valueBinders");
        if(valueBinders.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("valueBinders cannot contain null value");
        }

        this.valueBinders = valueBinders;
        return this;
    }

    @Override
    public String buildSql() {
        requireNotEmptyState(table, "table");
        requireNotEmptyState(columns, "fields");
        requireNotEmptyState(valueBinders, "valueBinders");
        if(columns.size() != valueBinders.size()) {
            throw new IllegalStateException("Columns and value sizes are not equal");
        }

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
