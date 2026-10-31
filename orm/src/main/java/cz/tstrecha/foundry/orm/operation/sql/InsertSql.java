package cz.tstrecha.foundry.orm.operation.sql;

import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import cz.tstrecha.foundry.orm.operation.result.BasicInsertOperationResult;
import cz.tstrecha.foundry.orm.operation.result.InsertOperationResult;
import cz.tstrecha.foundry.orm.operation.result.ReturningInsertOperationResult;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.*;

public class InsertSql implements ExecutableQuery<InsertOperationResult> {

    private String table;
    private Collection<String> columns;
    private List<ParameterBinder> valueBinders;
    @Getter
    private List<String> returningColumns;

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

    public InsertSql returning(List<String> returningColumns) {
        requireNotNullArgument(returningColumns, "returningColumns");

        if(returningColumns.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("returningColumns cannot contain null returningColumns");
        }

        this.returningColumns = returningColumns;
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
        if(returningColumns != null && !returningColumns.isEmpty()) {
            var returningColumnsStatement = String.join(",", returningColumns);
            sqlBuilder.append(" RETURNING ").append(returningColumnsStatement);
        }

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
    public InsertOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        if(returningColumns != null && !returningColumns.isEmpty()) {
            return new ReturningInsertOperationResult(statement, statement.executeQuery());
        } else {
            return new BasicInsertOperationResult(statement, statement.executeUpdate());
        }
    }
}
