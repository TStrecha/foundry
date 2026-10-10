package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.result.SelectOperationResult;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

@ToString
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
    public String buildSql() {
        var sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ").append(String.join(",", columns));
        sqlBuilder.append(" FROM ").append(table);

        if(filterBuilder != null) {
            sqlBuilder.append(" WHERE ").append(filterBuilder.getClause());
        }

        return sqlBuilder.toString();
    }

    @Override
    public void bindParameters(PreparedStatement statement) throws SQLException {
        if(filterBuilder != null) {
            for(int i = 0; i < filterBuilder.getParameters().size(); i++) {
                filterBuilder.getParameters().get(i).bind(statement, i + 1);
            }
        }
    }

    @Override
    public SelectOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new SelectOperationResult(statement, statement.executeQuery());
    }
}
