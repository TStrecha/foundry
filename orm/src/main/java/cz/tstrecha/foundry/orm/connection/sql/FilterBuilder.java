package cz.tstrecha.foundry.orm.connection.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FilterBuilder {

    private final StringBuilder clause = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();

    public FilterBuilder(String column, SqlOperator operator, Object value) {
        clause.append(recordParameterAndFormatClause(column, operator, value));
    }

    public FilterBuilder and(String column, SqlOperator operator, Object value) {
        clause.append(" AND ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    public FilterBuilder or(String column, SqlOperator operator, Object value) {
        clause.append(" OR ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    private String recordParameterAndFormatClause(String column, SqlOperator operator, Object value) {
        parameters.add(value);
        return String.format("\"%s\" %s ?", column, operator.getSqlOperator());
    }
}