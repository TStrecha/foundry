package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.executor.SelectSqlExecutor;
import cz.tstrecha.foundry.orm.connection.executor.SqlExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class SelectSql implements ExecutableQuery {

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
    public Query generateQuery() {
        var query = new StringBuilder();
        query.append(" SELECT ").append(String.join(",", columns));
        query.append(" FROM ").append(table);
        if(filterBuilder != null) {
            query.append(" WHERE ").append(filterBuilder.getClause());
        }

        return new Query(query.toString(), null);
    }

    @Override
    public SqlExecutor provideExecutor() {
        return new SelectSqlExecutor();
    }
}
