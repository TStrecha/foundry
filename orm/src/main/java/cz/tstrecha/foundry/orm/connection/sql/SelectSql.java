package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.connection.executor.SelectSqlExecutor;
import cz.tstrecha.foundry.orm.connection.executor.SqlExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SelectSql implements ExecutableQuery {

    private final Collection<String> columns;
    private String table;
    private FilterBuilder filterBuilder;

    public SelectSql from(String table) {
        this.table = table;
        return this;
    }

    public SelectSql where(String conditional, Consumer<FilterBuilder> whereBuilder) {
        var builder = new FilterBuilder(conditional);
        whereBuilder.accept(builder);
        this.filterBuilder = builder;
        return this;
    }

    public SelectSql where(String conditional) {
        this.filterBuilder = new FilterBuilder(conditional);;
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

    @Getter
    public static class FilterBuilder {
        private final StringBuilder clause = new StringBuilder();

        private FilterBuilder(String base) {
            clause.append(base);
        }

        public FilterBuilder and(String conditional) {
            clause.append(" AND ").append(conditional);
            return this;
        }

        public FilterBuilder or(String conditional) {
            clause.append(" OR ").append(conditional);
            return this;
        }
    }
}
