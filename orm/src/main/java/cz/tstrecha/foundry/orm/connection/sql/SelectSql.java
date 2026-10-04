package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.SelectOperationResult;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

@RequiredArgsConstructor
public class SelectSql implements ExecutableQuery<SelectOperationResult> {

    private final Collection<String> columns;
    private String table;
    private FilterBuilder filterBuilder;

    public SelectSql from(String table) {
        this.table = table;
        return this;
    }

    public SelectSql where(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    @Override
    public PreparedStatement generateStatement(Connection connection) throws SQLException {
        var sqlBuilder = new StringBuilder();
        sqlBuilder.append(" SELECT ").append(String.join(",", columns));
        sqlBuilder.append(" FROM ").append(table);
        if(filterBuilder != null) {
            sqlBuilder.append(" WHERE ").append(filterBuilder.getClause());
        }

        var preparedStatement = connection.prepareStatement(sqlBuilder.toString());
        if(filterBuilder != null) {
            for(int i = 0; i < filterBuilder.getParameters().size(); i++) {
                preparedStatement.setObject(i + 1, filterBuilder.getParameters().get(i));
            }
        }

        return preparedStatement;
    }

    @Override
    public SelectOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new SelectOperationResult(statement, statement.executeQuery());
    }
}
