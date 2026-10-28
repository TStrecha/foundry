package cz.tstrecha.foundry.orm.operation.sql;

import cz.tstrecha.foundry.orm.operation.filter.FilterBuilder;
import cz.tstrecha.foundry.orm.operation.result.SelectOperationResult;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.*;

@ToString
public class SelectSql implements ExecutableQuery<SelectOperationResult> {

    private final Collection<String> columns;
    private String table;
    private FilterBuilder filterBuilder;

    public SelectSql(Collection<String> columns) {
        requireNotEmptyArgument(columns, "columns");
        columns.forEach(column -> requireValidSqlIdentifier(column, "column"));

        this.columns = columns;
    }

    public SelectSql from(String table) {
        requireNotEmptyArgument(table, "table");
        requireValidSqlIdentifier(table, "table");

        this.table = table;
        return this;
    }

    public SelectSql where(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    @Override
    public String buildSql() {
        requireNotEmptyState(table, "table");
        requireNotEmptyState(columns, "columns");
        if(StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("Table name cannot be empty");
        }

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
