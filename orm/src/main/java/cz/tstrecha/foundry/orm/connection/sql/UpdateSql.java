package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.UpdateOperationResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.*;

public class UpdateSql implements ExecutableQuery<UpdateOperationResult> {

    private final String table;
    private Set<String> columnsToBeSet;
    private List<ParameterBinder> columnValueBinders;
    private FilterBuilder filterBuilder;

    public UpdateSql(String table) {
        requireNotEmptyArgument(table, "table");
        requireValidSqlIdentifier(table, "table");

        this.table = table;
    }

    public UpdateSql set(Map<String, ParameterBinder> valueSetters) {
        requireNotEmptyArgument(valueSetters, "valueSetters");
        valueSetters.keySet().forEach(column -> requireValidSqlIdentifier(column, "column"));

        this.columnsToBeSet = valueSetters.keySet();
        this.columnValueBinders = new ArrayList<>(valueSetters.values());
        return this;
    }

    public UpdateSql where(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    @Override
    public String buildSql() {
        requireNotEmptyState(columnsToBeSet, "columnsToBeSet");

        var setterStatement = columnsToBeSet.stream().map(column -> column + " = ?").collect(Collectors.joining(","));

        var sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(table);
        sqlBuilder.append(" SET ").append(setterStatement);

        if(filterBuilder != null) {
            sqlBuilder.append(" WHERE ").append(filterBuilder.getClause());
        }

        return sqlBuilder.toString();
    }

    @Override
    public void bindParameters(PreparedStatement statement) throws SQLException {
        requireNotEmptyState(columnValueBinders, "columnValueBinders");

        var i = 0;
        for(; i < columnValueBinders.size(); i++) {
            var valueBinder = columnValueBinders.get(i);
            valueBinder.bind(statement, i + 1);
        }

        if(filterBuilder != null) {
            for(; i < filterBuilder.getParameters().size() + columnValueBinders.size(); i++) {
                filterBuilder.getParameters().get(i - columnValueBinders.size()).bind(statement, i + 1);
            }
        }
    }

    @Override
    public UpdateOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new UpdateOperationResult(statement, statement.executeUpdate());
    }
}
