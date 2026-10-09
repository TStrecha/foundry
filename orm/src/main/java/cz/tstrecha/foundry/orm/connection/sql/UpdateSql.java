package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.UpdateOperationResult;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UpdateSql implements ExecutableQuery<UpdateOperationResult> {

    private final String table;
    private Set<String> columnsToBeSet;
    private List<ParameterBinder> columnValueBinders;
    private FilterBuilder filterBuilder;

    public UpdateSql set(Map<String, ParameterBinder> valueSetter) {
        this.columnsToBeSet = valueSetter.keySet();
        this.columnValueBinders = new ArrayList<>(valueSetter.values());
        return this;
    }

    public UpdateSql where(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    @Override
    public PreparedStatement generateStatement(Connection connection) throws SQLException {
        var setterStatement = columnsToBeSet.stream().map(column -> column + " = ?").collect(Collectors.joining(","));

        var sqlBuilder = new StringBuilder();
        sqlBuilder.append(" UPDATE ").append(table);
        sqlBuilder.append(" SET ").append(setterStatement);

        if(filterBuilder != null) {
            sqlBuilder.append(" WHERE ").append(filterBuilder.getClause());
        }

        var preparedStatement = connection.prepareStatement(sqlBuilder.toString());
        var i = 0;
        for(; i < columnValueBinders.size(); i++) {
            var valueBinder = columnValueBinders.get(i);
            valueBinder.bind(preparedStatement, i + 1);
        }

        if(filterBuilder != null) {
            for(; i < filterBuilder.getParameters().size() + columnValueBinders.size(); i++) {
                filterBuilder.getParameters().get(i - columnValueBinders.size()).bind(preparedStatement, i + 1);
            }
        }

        return preparedStatement;
    }

    @Override
    public UpdateOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new UpdateOperationResult(statement, statement.executeUpdate());
    }
}
