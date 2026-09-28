package cz.tstrecha.foundry.orm.connection.sql;

import lombok.Getter;

@Getter
public class FilterBuilder {

    private final StringBuilder clause = new StringBuilder();

    public FilterBuilder(String column, SqlOperator operator, Object value) {
        clause.append(formatOperation(column, operator, value));
    }

    public FilterBuilder and(String column, SqlOperator operator, Object value) {
        clause.append(" AND ").append(formatOperation(column, operator, value));
        return this;
    }

    public FilterBuilder or(String column, SqlOperator operator, Object value) {
        clause.append(" OR ").append(formatOperation(column, operator, value));
        return this;
    }

    private String formatOperation(String column, SqlOperator operator, Object value) {
        return String.format("\"%s\" %s '%s'", column, operator.getSqlOperator(), value.toString());
    }
}